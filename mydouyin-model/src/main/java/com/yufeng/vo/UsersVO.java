package com.yufeng.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Id;
import java.util.Date;

/**
 * @author Lzm
 * VO指的是View Object，表示视图对象，这个对象前端一般用于展示数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UsersVO {
    private String id;
    private String mobile;
    private String nickname;
    private String imoocNum;
    private String face;
    private Integer sex;
    private Date birthday;
    private String country;
    private String province;
    private String city;
    private String district;
    private String description;
    private String bgImg;
    private Integer canImoocNumBeUpdated;
    private Date createdTime;
    private Date updatedTime;

    // 添加前端所需要的UserToken
    private String userToken;
    // 添加前端所需要的关注数和粉丝数
    private Integer myFollowsCounts;
    private Integer myFansCounts;
    //    private Integer myLikedVlogCounts;
    // 获赞总数
    private Integer totalLikeMeCounts;
}