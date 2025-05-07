package com.yufeng.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

/**
 * @author Lzm
 * 这个BO对象是用来接收前端传过来的用户信息的，这些用户信息是在用户修改个人信息时需要更新的
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UpdatedUserBO {
    private String id;
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
}