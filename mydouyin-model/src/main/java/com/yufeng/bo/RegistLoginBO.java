package com.yufeng.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

/**
 * @author Lzm
 * @describe 这个类型叫做BO（Business Object），也就是业务对象，一般是前端的json对象，传去给后端
 * '@NotBlank'、'@Length是hibernate框架的注解，用来进行参数校验的注解
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RegistLoginBO {

    @NotBlank(message = "手机号不能为空")
    @Length(min = 11, max = 11, message = "手机长度不正确")
    private String mobile;

    @NotBlank(message = "验证码不能为空")
    private String smsCode;

}
