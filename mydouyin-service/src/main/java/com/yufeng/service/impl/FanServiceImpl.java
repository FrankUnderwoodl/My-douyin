package com.yufeng.service.impl;

import com.github.pagehelper.PageHelper;
import com.yufeng.base.BaseInfoProperties;
import com.yufeng.base.RabbitMQConfig;
import com.yufeng.enums.MessageEnum;
import com.yufeng.enums.YesOrNo;
import com.yufeng.mapper.FansMapper;
import com.yufeng.mapper.FansMapperCustom;
import com.yufeng.mo.MessageMO;
import com.yufeng.pojo.Fans;
import com.yufeng.service.FanService;
import com.yufeng.service.MsgService;
import com.yufeng.utils.JsonUtils;
import com.yufeng.utils.PagedGridResult;
import com.yufeng.vo.FansVO;
import com.yufeng.vo.VlogerVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lzm
 * @CreateTime 2025年5月11日 14:07
 */
@Slf4j
@Service
public class FanServiceImpl extends BaseInfoProperties implements FanService {

    // 这个mapper的作用是操作fans表
    @Autowired
    private FansMapper fanMapper;

    // 这个mapper的作用是操作fans表的自定义查询
    @Autowired
    private FansMapperCustom fansMapperCustom;

    // 注入消息服务，用于发送关注消息
    @Autowired
    MsgService msgService;

    // 注入MQ
    @Autowired
    private RabbitTemplate rabbitTemplate;

    // 注入Sid,可以生成唯一id主键
    @Autowired
    private Sid sid;

    /**
     * 目的是:关注当前视频的博主
     *
     * @param myId    我的id
     * @param vlogerId 博主id
     */
    @Transactional
    @Override
    public void doFollow(String myId, String vlogerId) {
        // 创建一条记录进数据库中，其实就是创建一个pojo类(你可以将Fans类比成fan表中的一条记录)
        Fans fans = new Fans();

        // 给这一条记录设置唯一的id，也就是主键
        String id = sid.nextShort();
        fans.setId(id);

        // 设置粉丝的id
        fans.setFanId(myId);

        // 设置博主的id
        fans.setVlogerId(vlogerId);

        // 设置粉丝是否是博主的朋友
        // fans.setIsFanFriendOfMine(1); // 这里可不能随便设置，得先查询数据库来判断对方(博主)是否有关注我(当前用户)
        Fans vloger = queryFansRelationship(vlogerId, myId);// 这里需要将myId和vlogerId对调一下(博主有没有关注我)
        if (vloger != null) { // 如果查到的记录不为空，说明博主已经关注我了
            // 将当前的记录的isFanFriendOfMine设置为1
            fans.setIsFanFriendOfMine(YesOrNo.YES.type);
            // 这里需要将对方的isFanFriendOfMine也设置为1
            vloger.setIsFanFriendOfMine(YesOrNo.YES.type);
            // 将对方的记录更新到数据库
            fanMapper.updateByPrimaryKeySelective(vloger);
        } else {
            // 如果为空，说明博主没有关注我
            fans.setIsFanFriendOfMine(YesOrNo.NO.type);
        }

        // 追加一条记录到MySQL中(当前用户关注了该视频博主这个动作)
        fanMapper.insert(fans);


        // 发送关注消息(其实底层就是向MongoDB中插入一条消息记录)
        // msgService.createMsg(myId, vlogerId, MessageEnum.FOLLOW_YOU.type, null);
        // 创建一个mo对象，用于发送消息到RabbitMQ
        MessageMO messageMO = new MessageMO();
        messageMO.setFromUserId(myId); // 设置发送者ID
        messageMO.setToUserId(vlogerId); // 设置接收者ID

        // 交给RabbitMQ生产消息(异步执行，不会阻塞当前线程)
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_MSG, // 要发送交换机的名称
                "sys.msg" + MessageEnum.FOLLOW_YOU.enValue, // 路由键(规定消息的路由规则，也就是交换机将消息发送到哪个队列)
                JsonUtils.objectToJson(messageMO)); // 消息具体内容
    }

    @Transactional
    @Override
    public void doCancel(String myId, String vlogerId) {
        // 首先查询一下我们是否为朋友关系，如果‘是’，就将对方记录里面的isFanFriendOfMine设置为0
        Fans fan = queryFansRelationship(myId, vlogerId);
        if(fan != null && fan.getIsFanFriendOfMine() == YesOrNo.YES.type) {
            // 将对方的isFanFriendOfMine设置为0
            Fans pendingFan = queryFansRelationship(vlogerId, myId);
            pendingFan.setIsFanFriendOfMine(YesOrNo.NO.type);
            // 更新对方的记录到数据库
            fanMapper.updateByPrimaryKeySelective(pendingFan);
        }

        // 取消关注当前视频的博主(其实就只需删除粉丝表中的‘我关注该博主’记录就行)
        fanMapper.delete(fan);
    }

    // 查询我是否关注了该博主
    @Override
    public boolean queryDoIFollowVloger(String myId, String vlogerId) {
        // 向数据库中的fans表中查询‘myId’是否有关注‘vlogerId’
        Fans fans = queryFansRelationship(myId, vlogerId);
        return fans != null;
    }



    @Override
    public PagedGridResult queryMyFollows(String myId, Integer page, Integer pageSize) {
        // 将传过来的参数放入一个map中
        Map<String, Object> map = new HashMap<>();
        map.put("myId", myId);

        // 先进行分页查询
        PageHelper.startPage(page, pageSize);

        // 调用dao层的查询方法，查询出当前用户关注的博主列表
        List<VlogerVO> list = fansMapperCustom.queryMyFollows(map);

        return setterPagedGrid(list, page);
    }


    @Override
    public PagedGridResult queryMyFans(String myId, Integer page, Integer pageSize) {
        /**
         * < 判断粉丝是否是我的朋友（前端实现互粉互关功能，也就是当前用户关注粉丝）>
         *  普通做法：
         *  多表关联+嵌套关联查询，这样会违反多表关联的规范，不可取，高并发下会出现性能问题
         *  ---
         *  常规做法：
         *  1. 避免过多的表关联查询，先查询我的粉丝列表，获得fansList
         *  2. 遍历fansList，查询每个粉丝是否是我的朋友
         *  3. 如果是朋友关系，则将isFanFriendOfMine设置为1，否则设置为0
         *  ---
         *  高端做法：
         *  1. 在关注/取关的时候，关联关系就保存在redis中，不要依赖数据库
         *  2. 数据库查询后，直接循环查询Redis，避免第二次查询数据库的尴尬局面
         */

        // 将传过来的参数放入一个map中
        Map<String, Object> map = new HashMap<>();
        map.put("myId", myId);

        // 先进行分页查询
        PageHelper.startPage(page, pageSize);

        // 调用dao层的查询方法，查询出当前用户关注的粉丝列表
        List<FansVO> list = fansMapperCustom.queryMyFans(map);

        // 因为查出来的粉丝有可能是我的朋友，所以需要遍历一下(这里就可以借助redis来判断，而不用每个vo对象都去查询数据库)
        for (FansVO fansVO : list) {
            String relationship = redis.get(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + myId + ":" + fansVO.getFanId());

            if(StringUtils.isNotBlank(relationship) && relationship.equalsIgnoreCase("1")) {
                // 如果不为空，说明我和粉丝是朋友关系
                fansVO.setFriend(true);
            } else {
                // 如果为空，说明我和粉丝不是朋友关系
                // fansVO.setFriend(false); // 默认就为false，所以可以不设置
            }

        }

        return setterPagedGrid(list, page);
    }


    /**
     * 作用：查询某个用户是否关注了某个博主，如果在fans表中查到了记录，说明关注了，否则没有关注
     * 这段代码的底层SQL其实是：SELECT * FROM fans WHERE fan_id = #{myId} AND vloger_id = #{vlogerId}
     */
    private Fans queryFansRelationship(String myId, String vlogerId) {
        // 查询fans表，查询‘myID’是否有关注‘vlogerId’

        // 构建一个Example对象，用于查询(创建一个针对Fans表的查询构建器)
        Example example = new Example(Fans.class);
        // 设置查询条件
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("fanId", myId); // 添加条件：fan_id = myId
        criteria.andEqualTo("vlogerId", vlogerId);  // 添加条件：vloger_id = vlogerId

        // 执行查询，查看数据中是否有符合条件的记录
        List<Fans> list = fanMapper.selectByExample(example);
        // 判断查询结果是否为空
        if (list != null && !list.isEmpty()) {
            // 如果不为空，说明已经关注
            return list.get(0);
        }
        return null;
    }
}
