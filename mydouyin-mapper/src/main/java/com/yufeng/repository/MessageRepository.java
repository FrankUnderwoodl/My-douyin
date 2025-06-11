package com.yufeng.repository;

import com.yufeng.mo.MessageMO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Lzm
 * @CreateTime 2025年5月24日 22:00
 * 继承MongoRepository后，你的MessageRepository接口自动获得了以下功能：
 * 基本的CRUD操作（创建、读取、更新、删除）
 * 分页和排序功能
 * 按照方法名自动生成查询功能
 * 自定义查询支持
 */
@Repository
public interface MessageRepository extends MongoRepository<MessageMO,String> {

    // 通过实现Repository，自定义条件查询
    // 根据接收者用户ID查询消息列表，并按创建时间降序排列
    List<MessageMO> findAllByToUserIdOrderByCreateTimeDesc(String toUserId,
                                                           Pageable pageable);
}
