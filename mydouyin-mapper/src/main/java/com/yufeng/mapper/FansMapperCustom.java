package com.yufeng.mapper;

import com.yufeng.my.mapper.MyMapper;
import com.yufeng.pojo.Fans;
import com.yufeng.vo.FansVO;
import com.yufeng.vo.VlogerVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface FansMapperCustom extends MyMapper<Fans> {

    /**
     * 查询我关注的博主
     */
    public List<VlogerVO> queryMyFollows(@Param("paramMap")Map<String,Object> map);

    /**
     * 查询我的粉丝
     */
    public List<FansVO> queryMyFans(@Param("paramMap")Map<String,Object> map);
}