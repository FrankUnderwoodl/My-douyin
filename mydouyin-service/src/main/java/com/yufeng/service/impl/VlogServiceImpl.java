package com.yufeng.service.impl;

import com.github.pagehelper.PageHelper;
import com.yufeng.base.BaseInfoProperties;
import com.yufeng.base.RabbitMQConfig;
import com.yufeng.bo.VlogBO;
import com.yufeng.enums.MessageEnum;
import com.yufeng.enums.YesOrNo;
import com.yufeng.grace.result.GraceJSONResult;
import com.yufeng.mapper.MyLikedVlogMapper;
import com.yufeng.mapper.VlogMapper;
import com.yufeng.mapper.VlogMapperCustom;
import com.yufeng.mo.MessageMO;
import com.yufeng.pojo.MyLikedVlog;
import com.yufeng.pojo.Vlog;
import com.yufeng.service.FanService;
import com.yufeng.service.MsgService;
import com.yufeng.service.VlogService;
import com.yufeng.utils.JsonUtils;
import com.yufeng.utils.PagedGridResult;
import com.yufeng.vo.IndexVlogVO;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

/**
 * @author Lzm
 * @CreateTime 2025年5月09日 21:43
 */
@Service
public class VlogServiceImpl extends BaseInfoProperties implements VlogService {

    @Autowired
    private Sid sid;

    @Autowired
    private VlogMapper vlogMapper;

    @Autowired
    private VlogMapperCustom vlogMapperCustom;

    @Autowired
    private MyLikedVlogMapper myLikedVlogMapper;

    @Autowired
    private MsgService msgService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private FanService fanService;


    /**
     * 作用：向数据库中存入一条视频记录
     */
    @Override
    public void createVlog(VlogBO vlogBO) {

        // 生成一个唯一的id，通过推特的snowflake算法生成
        String vid = sid.nextShort();

        Vlog vlog = new Vlog();
        // 将VlogBO中的数据拷贝到Vlog中(将BO变为POJO)
        BeanUtils.copyProperties(vlogBO, vlog);
        // 然后对vlog这个pojo进行常规的赋值
        vlog.setId(vid);
        vlog.setLikeCounts(0);
        vlog.setCommentsCounts(0);
        vlog.setIsPrivate(YesOrNo.NO.type);

        vlog.setCreatedTime(new Date());
        vlog.setUpdatedTime(new Date());

        // 插入到数据库中(Dao层)
        vlogMapper.insert(vlog);
    }


    @Override
    public PagedGridResult getIndexVlogList(String userId,
                                            String search,
                                            Integer page,
                                            Integer pageSize) {
        /**
         * PageHelper 通过 MyBatis 的拦截器机制（实现了 Interceptor 接口）拦截 SQL 执行
         * 当检测到 ThreadLocal 中有分页参数时，会在原始 SQL 的基础上进行改写
         * 例如对于 MySQL，它会将：
         * SELECT * FROM user
         * 改写为：
         * SELECT * FROM user LIMIT ?, ?
         * 其中问号会被替换为分页的起始位置和每页大小
         */
        PageHelper.startPage(page, pageSize);

        HashMap<String, Object> map = new HashMap<>();
        // 判断search是否为空，如果不为空，那就是搜索页了(默认搜索页的底层具体是dao层通过模糊查询来实现)
        if (StringUtils.isNotBlank(search)) {
            map.put("search", search); // 传入参数给dao层
        }

        // 通过dao层查询出所有适合的vlog
        // 这里直接返还装有IndexVlogVo的List可不行，因为前端还需要知道有几页、总共数据库查出了有多少条记录、当前是第几页...，所以需要对List<IndexVlogVO>进行再封装成一个PagedGridResult对象
        List<IndexVlogVO> vlogList = vlogMapperCustom.getIndexVlogList(map);


        // 现在需要循环每个vo，因为前端需要知道该视频用户是否有点赞、该视频的点赞总数、评论总数、还有该用户是否有关注视频发布者
        for (IndexVlogVO vo : vlogList) {
            // 获取当前视频的主键
            String vlogId = vo.getVlogId();
            // 获取当前视频的发布者
            String vlogerId = vo.getVlogerId();


            // 一. 查询数据库中的fans表，判断当前用户是否点赞了该视频
            // ①通过userId和vlogId来查询出那条记录(直接查数据库)
            /* Example example = new Example(MyLikedVlog.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("userId", vo.getUserId());
            criteria.andEqualTo("vlogId", vo.getVlogId());
            // 如果有记录，则说明该视频被点赞了
            List<MyLikedVlog> myLikedVlogList = myLikedVlogMapper.selectByExample(example);
            if (myLikedVlogList != null && !myLikedVlogList.isEmpty()) {
                vo.setIsLike(YesOrNo.YES.type); // 该视频被点赞了
            } else {
                vo.setIsLike(YesOrNo.NO.type); // 该视频没有被点赞
            } */

            // 一. 上面的方式是直接查询数据库来操作，但是这样太low、也太慢了(因为每条记录、每个对象都需要向数据库查询)，用Redis
            if (StringUtils.isNotBlank(userId)) {
                // ①通过Redis的key来判断该视频是否被当前用户点赞了(REDIS_USER_LIKE_VLOG)
                vo.setDoILikeThisVlog(this.doILikeVlog(userId, vlogId));
                // ②查询当前用户是否关注了视频发布者(通过fanService来查询)
                boolean b = fanService.queryDoIFollowVloger(userId, vlogerId);
                vo.setDoIFollowVloger(b);
            }

            // 二. 查询该视频的点赞总数(REDIS_VLOG_BE_LIKED_COUNTS)
            vo.setLikeCounts(getVlogBeLikedCounts(vlogId));

        }

        return setterPagedGrid(vlogList, page);
    }

    // 查询用户是否点赞了该视频(因为点赞的时候会将点赞记录存入Redis中，所以这里直接查询Redis就行了，当然也可以查询数据库)
    private boolean doILikeVlog(String myId, String vlogId) {
        // 查询Redis
        String doILike = redis.get(REDIS_USER_LIKE_VLOG + ":" + myId + ":" + vlogId);

        boolean isLike = false;
        if(StringUtils.isNotBlank(doILike) && doILike.equalsIgnoreCase("1")) {
            isLike = true;
        }
        return isLike;
    }


    // 查询某个视频的点赞总数(因为点赞的时候会将点赞记录存入Redis中去自增，所以这里直接查询Redis就行了，当然也可以查询数据库)
    @Override
    public Integer getVlogBeLikedCounts(String vlogId) {
        // 查询Redis
        String likeCounts = redis.get(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId);
        if (StringUtils.isBlank(likeCounts) || likeCounts.equalsIgnoreCase("0")) {
            return 0;
        } else {
            return Integer.valueOf(likeCounts);
        }
    }


    // 查询当前用户的所有点赞视频
    @Override
    public PagedGridResult getMyLikedVlogList(String userId, Integer page, Integer pageSize) {
        // 开启分页
        PageHelper.startPage(page,pageSize);

        // 查询出所有的点赞视频(dao层)
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        List<IndexVlogVO> list = vlogMapperCustom.getMyLikedVlogList(map);

        return setterPagedGrid(list,page);
    }


    // 查询当前用户关注博主发布的视频列表
    @Override
    public PagedGridResult getMyFollowVlogList(String myId, Integer page, Integer pageSize) {
        // 开启分页
        PageHelper.startPage(page, pageSize);

        // 查询出所有的关注博主所发布的视频(dao层)
        HashMap<String, Object> map = new HashMap<>();
        map.put("myId", myId);
        List<IndexVlogVO> list = vlogMapperCustom.getMyFollowVlogList(map);

        // 现在需要循环每个vo，因为前端需要知道每个视频vo用户是否有点赞、点赞总数、评论总数
        for (IndexVlogVO vo : list) {
            // 获取当前视频的主键
            String vlogId = vo.getVlogId();

            // 一. 查询当前用户是否点赞了该视频
            if (StringUtils.isNotBlank(myId)) {
                // ①设置当前的视频，用户是否有点赞
                vo.setDoILikeThisVlog(this.doILikeVlog(myId, vlogId));
                // ②设置当前视频的作者，用户是否有关注(肯定有关注的，因为是关注博主发布的视频)
                vo.setDoIFollowVloger(true);
            }

            // 二. 查询该视频的点赞总数
            vo.setLikeCounts(this.getVlogBeLikedCounts(vlogId));
        }


        return setterPagedGrid(list, page);
    }



    @Override
    public PagedGridResult getMyFriendVlogList(String myId, Integer page, Integer pageSize) {
        // 开启分页
        PageHelper.startPage(page, pageSize);

        // 查询出所有朋友所发布的视频(dao层)
        HashMap<String, Object> map = new HashMap<>();
        map.put("myId", myId);
        List<IndexVlogVO> list = vlogMapperCustom.getMyFriendVlogList(map);

        // 现在需要循环每个vo，因为前端需要知道每个视频vo用户是否有点赞、点赞总数、评论总数
        for (IndexVlogVO vo : list) {
            // 获取当前视频的主键
            String vlogId = vo.getVlogId();

            // 一. 查询当前用户是否点赞了该视频
            if (StringUtils.isNotBlank(myId)) {
                // ①设置当前的视频，用户是否有点赞
                vo.setDoILikeThisVlog(this.doILikeVlog(myId, vlogId));
                // ②设置当前视频的作者，用户是否有关注(肯定有关注的，因为是关注博主发布的视频)
                vo.setDoIFollowVloger(true);
            }

            // 二. 查询该视频的点赞总数
            vo.setLikeCounts(this.getVlogBeLikedCounts(vlogId));
        }
        return setterPagedGrid(list, page);
    }


    // 把该视频用户是否有点赞、该视频的点赞总数、评论总数、还有该用户是否有关注视频发布者给封装到一个方法中
    private IndexVlogVO setterVlogVo(IndexVlogVO vo, String userId) {

        // 一. 查询当前用户是否点赞了该视频、是否有关注视频发布者
        if (StringUtils.isNotBlank(userId)) {
            // ①设置当前的视频，用户是否有点赞
            vo.setDoILikeThisVlog(this.doILikeVlog(userId, vo.getVlogId()));
            // ②设置当前视频的作者，用户是否有关注
            vo.setDoIFollowVloger(this.doILikeVlog(userId, vo.getVlogerId()));
        }
        // 二. 查询该视频的点赞总数
        vo.setLikeCounts(this.getVlogBeLikedCounts(vo.getVlogId()));
        return vo;
    }



    // 查询某个视频的详情(不含tab页)
    @Override
    public IndexVlogVO getVlogDetailById(String userId,String vlogId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("vlogId", vlogId);

        // 直接调用dao层来操纵数据库
        List<IndexVlogVO> vlogList = vlogMapperCustom.getVlogDetailById(map);

        if (vlogList != null && !vlogList.isEmpty()) {
            // 获得一个视频vo
            IndexVlogVO vo = vlogList.get(0);
            // 然后对vo进行常规的赋值(用户是否有点赞、该视频的点赞总数、评论总数、还有该用户是否有关注视频发布者)
            return setterVlogVo(vo, userId);
        } else {
            return null;
        }

    }

    @Override
    @Transactional
    public void changeToPrivateOrPublish(String userId, String vlogerId, Integer yesOrNo) {
        // 不推荐使用主键更新，因为没有userId的话，一个请求过来的话，那不就所有的用户都能更改了吗？

        // 通过视频的具体id和上传这条视频的用户id来找到粉丝表中的那条记录
        Example example = new Example(Vlog.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id", vlogerId); // 视频id
        criteria.andEqualTo("vlogerId", userId); // 上传这条视频的用户id

        Vlog pendingVlog = new Vlog();
        pendingVlog.setIsPrivate(yesOrNo); // 公开/私密

        // 更新到数据库中
        vlogMapper.updateByExampleSelective(pendingVlog, example); // example是找到那条记录，而pendingVlog则是要更新的记录(找到记录，更新记录)

    }

    @Override
    public PagedGridResult queryMyVlogList(String userId, Integer page, Integer pageSize, Integer yesOrNo) {
        // 查询vlog中，该user的所有视频
        Example example = new Example(Vlog.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("vlogerId", userId); // 在vlog表中，通过用户id来查询所有的vlog
        criteria.andEqualTo("isPrivate", yesOrNo); // 是私密的还是公开的视频

        // 开启分页
        PageHelper.startPage(page, pageSize);
        // 查询出所有的vlog
        List<Vlog> vlogList = vlogMapper.selectByExample(example);

        return setterPagedGrid(vlogList, page);
    }


    @Transactional
    @Override
    public void userLikeVlog(String userId, String vlogId) {
        // 生成一个唯一的id，通过推特的snowflake算法生成
        String sid = this.sid.nextShort();

        // 新建一条记录
        MyLikedVlog myLikedVlog = new MyLikedVlog();
        myLikedVlog.setId(sid);
        myLikedVlog.setUserId(userId);
        myLikedVlog.setVlogId(vlogId);

        // 调用dao层的方法，向MySQL中的my_liked_vlog中插入一条记录(id｜user_id｜vlog_id)-> 某个用户点赞了某个视频
        myLikedVlogMapper.insert(myLikedVlog);


        // 这里假如发生一个异常，这样MySQL就会回滚，也就是说上面插入的记录就会被删除(解决方案就是使用RabbitMQ来异步处理消息，这样即使发生异常，MySQL中的记录也不会被删除)
        // throw new RuntimeException("模拟事务回滚");



        // 系统消息：点赞短视频的信息(其实底层是向MongoDB中插入一条记录)
        // ①可以通过vlogMapper获取视频的封面、主键，然后放入Map中(在MongoDB中是一个json对象)中
        Vlog vlog = this.getVlog(vlogId);
        Map<Object, Object> msgContent = new HashMap<>();
        msgContent.put("vlogId", vlogId);
        msgContent.put("vlogCover", vlog.getCover());

        // ②调用MsgService来创建一条消息(某个用户点赞了某个视频)
        // msgService.createMsg(userId, vlog.getVlogerId(), MessageEnum.LIKE_VLOG.type, msgContent);

        // ③创建一个mo对象，用于发送消息到RabbitMQ进行异步处理
        MessageMO messageMO = new MessageMO();
        messageMO.setFromUserId(userId); // 设置发送者ID
        messageMO.setToUserId(vlog.getVlogerId()); // 设置接收者ID
        messageMO.setMsgContent(msgContent); // 设置消息内容
        // ④交给RabbitMQ生产消息(异步执行，不会阻塞当前线程)
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_MSG, // 要发送交换机的名称
                "sys.msg" + MessageEnum.LIKE_VLOG.enValue, // 路由键(规定消息的路由规则，也就是交换机将消息发送到哪个队列)
                JsonUtils.objectToJson(messageMO)); // 消息具体内容

    }

    @Override
    public void flushCountsToMySQL(String vlogId, Integer counts) {
        // 实现思路①：通过vlogId来查询出那条记录，然后更新那条记录的点赞数
        // // 通过vlogId来查询出那条记录
        // Example example = new Example(Vlog.class);
        // Example.Criteria criteria = example.createCriteria();
        // criteria.andEqualTo("id", vlogId);
        //
        // // 更新那条记录的点赞数
        // Vlog vlog = new Vlog();
        // vlog.setLikeCounts(counts); // 设置点赞数
        //
        // // 更新到数据库中
        // vlogMapper.updateByExampleSelective(vlog, example);


        // 实现思路②：直接通过vlogId来更新那条记录的点赞数
        Vlog vlog = new Vlog();
        vlog.setId(vlogId); // 设置视频的主键id
        vlog.setLikeCounts(counts); // 设置点赞数
        vlog.setUpdatedTime(new Date()); // 设置更新时间
        // 更新到数据库中
        vlogMapper.updateByPrimaryKeySelective(vlog); // 通过主键id来更新那条记录的点赞数


    }

    // 通过主键id查询某个视频
    @Override
    public Vlog getVlog(String id) {
        return vlogMapper.selectByPrimaryKey(id);
    }

    @Transactional
    @Override
    public void userUnLikeVlog(String userId, String vlogId) {
        // 通过userId和vlogId来查询出那条记录
        Example example = new Example(MyLikedVlog.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("userId", userId);
        criteria.andEqualTo("vlogId", vlogId);

        // 删除那条记录
        myLikedVlogMapper.deleteByExample(example);
    }
}
