package com.yufeng.service.impl;

import com.yufeng.bo.UpdatedUserBO;
import com.yufeng.enums.Sex;
import com.yufeng.enums.UserInfoModifyType;
import com.yufeng.enums.YesOrNo;
import com.yufeng.exceptions.GraceException;
import com.yufeng.grace.result.ResponseStatusEnum;
import com.yufeng.mapper.UsersMapper;
import com.yufeng.pojo.Users;
import com.yufeng.service.UserService;
import com.yufeng.utils.DateUtil;
import com.yufeng.utils.DesensitizationUtil;
import org.apache.catalina.User;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;

/**
 * @author Lzm
 * @CreateTime 2025年5月07日 14:07
 */
@Service
public class UserServiceImpl implements UserService {

    // 自动注入一个 UsersMapper 接口，这里就能提现spring的强大，不用你手动创建对象
    @Autowired
    private UsersMapper usersMapper;

    // 自动注入一个 Sid 对象，用于生成唯一的ID
    @Autowired
    private Sid sid;

    // 用户头像的默认值
    private static final String USER_FACE1 = "https://img2.baidu.com/it/u=885277635,546855475&fm=253&fmt=auto&app=138&f=JPEG?w=760&h=760";


    /**
     * 目的是: 根据手机号，在MySQL中查询用户是否存在
     */
    @Override
    public Users queryMobileIsExist(String mobile) {

        // 这里创建了一个针对 Users 类的查询条件对象Example
        Example usersExample = new Example(Users.class);

        // Criteria 是 Example 内部的一个类，用于设置具体的查询条件
        Example.Criteria criteria = usersExample.createCriteria();

        // 这行代码设置了一个等值查询条件：mobile 字段等于传入的 mobile 参数
        // 相当于 SQL 中的 WHERE mobile = ?
        criteria.andEqualTo("mobile", mobile);

        // 使用 usersMapper 的 selectOneByExample 方法执行查询
        // 该方法期望只返回一条记录，如果有多条会返回第一条
        // 这段代码相当于SQL中的什么？
        // SELECT * FROM users WHERE mobile = '传入的手机号值' LIMIT 1
        Users user = usersMapper.selectOneByExample(usersExample);
        return user;
    }



    /**
     * 目的是: 创建用户
     */
    @Transactional // 因为涉及到数据库的插入操作，所以需要事务管理
    @Override
    public Users createUser(String mobile) {
        // 获得全局唯一主键
        String userId = sid.nextShort();

        Users user = new Users();
        user.setId(userId);

        user.setMobile(mobile);
        user.setNickname("用户：" + DesensitizationUtil.commonDisplay(mobile));
        user.setImoocNum("用户：" + DesensitizationUtil.commonDisplay(mobile));
        user.setFace(USER_FACE1);

        user.setBirthday(DateUtil.stringToDate("1900-01-01"));
        user.setSex(Sex.secret.type);

        user.setCountry("中国");
        user.setProvince("");
        user.setCity("");
        user.setDistrict("");
        user.setDescription("这家伙很懒，什么都没留下~");
        user.setCanImoocNumBeUpdated(YesOrNo.YES.type);

        user.setCreatedTime(new Date());
        user.setUpdatedTime(new Date());

        usersMapper.insert(user);

        // 返回插入后的用户对象
        return user;
    }


    @Override
    public Users getUser(String userId) {
        return usersMapper.selectByPrimaryKey(userId);
    }

    /**
     * 事务管理对于数据库更新操作非常重要，我来解释一下底层工作原理：
     * <p>
     * ## 为什么需要事务管理？
     * <p>
     * 事务管理确保了数据库操作的原子性、一致性、隔离性和持久性（ACID特性）：
     * <p>
     * 1. **原子性(Atomicity)**: 确保一系列操作要么全部完成，要么完全不执行
     * 2. **一致性(Consistency)**: 确保数据库从一个一致状态转变为另一个一致状态
     * 3. **隔离性(Isolation)**: 确保并发事务之间互不干扰
     * 4. **持久性(Durability)**: 确保已提交的事务永久保存
     * <p>
     * ## `@Transactional`底层工作过程
     * <p>
     * 当你在方法上添加`@Transactional`注解时，底层工作流程如下：
     * <p>
     * 1. **代理创建**：
     *    - Spring使用AOP（面向切面编程）创建一个代理对象包装你的服务类
     *    - 这个代理拦截对标注了`@Transactional`的方法的调用
     * <p>
     * 2. **事务开始**：
     *    - 当调用带有`@Transactional`的方法时，事务管理器创建一个事务
     *    - 获取数据库连接并设置为手动提交模式（`connection.setAutoCommit(false)`）
     * <p>
     * 3. **方法执行**：
     *    - 执行业务方法中的代码
     *    - 所有数据库操作都在同一个事务中进行
     * <p>
     * 4. **事务提交/回滚**：
     *    - 如果方法正常完成，事务管理器调用`connection.commit()`提交事务
     *    - 如果方法抛出异常，事务管理器调用`connection.rollback()`回滚事务
     *    - 这保证了要么所有数据库操作都成功执行，要么全部撤销，保持数据一致性
     * <p>
     * 5. **资源释放**：
     *    - 最后释放数据库连接和其他资源
     * <p>
     * 在你的`UpdateUserInfo`方法中，当你更新用户信息时，可能涉及多个操作（更新多个字段或甚至多个表），事务确保这些操作作为一个整体要么成功要么失败，保持数据的一致性。
     * 如果不使用事务管理，默认情况下，JDBC连接处于自动提交模式，每个SQL语句执行后会立即提交到数据库，这样可能导致数据不一致的状态。
     */
    @Transactional // 因为需要涉及到数据库的更新操作，所以需要事务管理
    @Override
    public Users UpdateUserInfo(UpdatedUserBO updatedUserBO) {
        Users pendingUser = new Users(); // pending的意思是“待处理的”
        BeanUtils.copyProperties(updatedUserBO, pendingUser); // 将updatedUserBO的值赋值给pendingUser，因为User对应了数据库中的表，所以需要copyProperties。

        // 接下来通过userMapper的updateByPrimaryKeySelective方法来更新数据库中的用户信息
        int result = usersMapper.updateByPrimaryKeySelective(pendingUser); // 只更新非空字段，也就是该对象如果有字段为null就不更新进数据库中

        // 这里的result是更新操作影响的行数，如果result大于0，说明更新成功
        if (result == 0) {
            // 如果更新失败，抛出异常
            GraceException.display(ResponseStatusEnum.USER_UPDATE_ERROR);

        }

        return getUser(updatedUserBO.getId());
    }


    /**
     * 根据传入的type类型，来判断是修改头像还是背景图片
     * @param updatedUserBO 客户端传来的对象
     * @param type 修改类型(上传头像还是背景图片)
     */
    @Override
    public Users UpdateUserInfo(UpdatedUserBO updatedUserBO, Integer type) {
        // 这里创建了一个专门针对 Users 类的查询条件对象Example
        Example example = new Example(Users.class);
        Example.Criteria criteria = example.createCriteria();

        // 判断type类型是否正确
        if (type == UserInfoModifyType.NICKNAME.type) {
            criteria.andEqualTo("nickname", updatedUserBO.getNickname());
            Users user = usersMapper.selectOneByExample(example);
            if (user != null) { // 如果查询到有相同的昵称，就抛出异常
                GraceException.display(ResponseStatusEnum.USER_INFO_UPDATED_NICKNAME_EXIST_ERROR);
            }
        }

        //
        if (type == UserInfoModifyType.IMOOCNUM.type) {
            criteria.andEqualTo("imoocNum", updatedUserBO.getImoocNum());
            Users user = usersMapper.selectOneByExample(example);
            if (user != null) { // 如果查询到有相同的imoocNum，就抛出异常
                GraceException.display(ResponseStatusEnum.USER_INFO_UPDATED_NICKNAME_EXIST_ERROR);
            }


            Users tempUser =  getUser(updatedUserBO.getId());
            if (tempUser.getCanImoocNumBeUpdated() == YesOrNo.NO.type) {
                GraceException.display(ResponseStatusEnum.USER_INFO_CANT_UPDATED_IMOOCNUM_ERROR);
            }
            // 把canImoocNumBeUpdated的值改为NO
            updatedUserBO.setCanImoocNumBeUpdated(YesOrNo.NO.type);
        }

        // 这里再将updatedUserBO的值赋值给pendingUser↓
        return this.UpdateUserInfo(updatedUserBO);
    }

}
