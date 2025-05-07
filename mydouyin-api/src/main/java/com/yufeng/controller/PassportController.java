package com.yufeng.controller;

import com.yufeng.bo.RegistLoginBO;
import com.yufeng.grace.result.GraceJSONResult;
import com.yufeng.grace.result.ResponseStatusEnum;
import com.yufeng.pojo.Users;
import com.yufeng.service.UserService;
import com.yufeng.service.impl.UserServiceImpl;
import com.yufeng.utils.IPUtil;
import com.yufeng.utils.SMSUtils;
import com.yufeng.vo.UsersVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import tk.mybatis.mapper.util.StringUtil;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Lzm
 * @CreateTime 2025年5月05日 18:05
 */

@Slf4j
@Api(tags = "Passport通行证接口") // 这个注解可以用来定义文档中Controller的名字
@RestController
@RequestMapping("/passport")
public class PassportController extends BaseInfoProperties {

    // 这个属性不用你手动去创建对象，spring会自动帮你创建一个对象，这就是spring的强大之处
    @Autowired
    private SMSUtils smsUtils;

    // 这里虽然是接口，但是spring会自动创建一个实现类对象，厉害厉害
    @Autowired
    private UserService userService;



    /**
     * 这个对应前端的getCode函数
     */
    @PostMapping("/getSMSCode")
    public Object getSMSCode(@RequestParam String mobile, HttpServletRequest request) throws Exception {
        // 1. 判断手机号是否为空
        if(mobile == null || mobile.isEmpty()) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.valueOf("手机号不能为空"));
        }

        // 2. 判断手机号格式是否正确
        if(!mobile.matches("^[1][3-9][0-9]{9}$")) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.valueOf("手机号格式不正确"));
        }

        // 3. 发送短信验证码，这里验证码可以使用随机数生成
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));

        // 获取用户的IP地址
        String userIp = IPUtil.getRequestIp(request);
        log.info("用户的IP地址: " + userIp);

        // 根据用户IP进行限制，限制用户在60秒之内只能获得一次验证码，nx 是 "not exists" 的缩写
        Boolean isAllowed = redis.setnx60s(MOBILE_SMSCODE + ":" + userIp, userIp);
        // 下面的代码可以用interceptor来实现
        // if(!isAllowed) {
        //     return GraceJSONResult.errorCustom(ResponseStatusEnum.SMS_NEED_WAIT_ERROR);
        // }

        // 把验证码放入到Redis中，用于后续的验证(限时为30分钟)
        redis.set(MOBILE_SMSCODE + ":" + mobile, code, 30 * 60);

        // 4. 这里可以使用短信服务提供商的SDK发送短信
        log.info("发送短信验证码: " + code + " 到手机号: " + mobile);

        // 5. 调用短信服务
        // smsUtils.sendSMS(mobile, code);

        // 6. 返回成功的结果，这里的底层会将GraceJSONResult对象转换为JSON格式
        // public GraceJSONResult(Object data) {
        //         this.status = ResponseStatusEnum.SUCCESS.status();
        //         this.msg = ResponseStatusEnum.SUCCESS.msg();
        //         this.success = ResponseStatusEnum.SUCCESS.success();
        //         this.data = data;
        //     }
        return GraceJSONResult.ok("短信发送成功");
    }


    /**
     * @describe 对应前端的loginOrRegist函数，目的是通过手机号和验证码来一键登录
     * @param registLoginBO 由前端传入的对象
     * @Valid 这个注解是用来‘开启验证对象’，也就是判断前端传来的RegistLoginBO对象是否符合要求
     */
    @PostMapping("/login")
    public Object login(@Valid @RequestBody RegistLoginBO registLoginBO,
                        // BindingResult result,   // 对代码有侵入性，因为当前的Controller需要依赖其他方法的结果。解决的方法就是通过这个BlindingResult如果有错误，就自动抛异常，就可以被我的GraceExceptionHandler捕获
                        HttpServletRequest request) {

        // 0. 判断BindingResult中是否保存了错误的验证信息，如果有，则需要返回到前端
       // if( result.hasErrors() ) {
       //     Map<String, String> map = getErrors(result);
       //     return GraceJSONResult.errorMap(map);
       // }

        // 0.获取registLoginBO对象中的手机号和验证码
        String mobile = registLoginBO.getMobile();
        String verifyCode = registLoginBO.getSmsCode();

        // 1.从Redis中获取验证码 并进行验证
        String redisCode = redis.get(MOBILE_SMSCODE + ":" + mobile);
        if(StringUtils.isBlank(redisCode) || !StringUtils.equalsIgnoreCase(redisCode, verifyCode)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SMS_CODE_ERROR);
        }

        // 2.验证码验证成功后，通过查询数据库，判断手机号是否存在，如果存在，则登录，如果不存在，则注册
        Users user = userService.queryMobileIsExist(mobile);
        if(user == null) {
            // 2.1如果不存在，则注册
            user = userService.createUser(mobile);
        }

        // 如果存在或者注册进数据库后，可以继续下方的业务，可以保存用户的信息和会话消息
        // 3.创建一个随机的、全局唯一的字符串作为用户的访问令牌(token)
        String uToken = UUID.randomUUID().toString();   // 生成一个token
        redis.set(REDIS_USER_TOKEN+ ":" + user.getId(), uToken); // 这里将是Token存入Redis中，过期时间为无限

        // 4.用户登陆成功后，顺便删除Redis中的验证码
        redis.del(MOBILE_SMSCODE + ":" + mobile);

        // 5. 返回用户信息和token给前端
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(user, usersVO); // 将user对象中的属性复制到usersVO对象中
        usersVO.setUserToken(uToken); // 设置用户的token

        return GraceJSONResult.ok(usersVO);
    }



    @PostMapping("logout")
    public GraceJSONResult logout(@RequestParam String userId,
                                  HttpServletRequest request) throws Exception {

        // 后端只需要清除与用户会话的token信息即可，当然前端也需要清除，清除本地app中的用户信息和token会话信息
        redis.del(REDIS_USER_TOKEN + ":" + userId);

        return GraceJSONResult.ok();
    }

   //  public Map<String, String> getErrors(BindingResult result) {
   //     Map<String, String> map = new HashMap<>();
   //     List<FieldError> errorList = result.getFieldErrors();
   //     for (FieldError ff : errorList) {
   //         // 错误所对应的属性字段名
   //         String field = ff.getField();
   //         // 错误的信息
   //         String msg = ff.getDefaultMessage();
   //         map.put(field, msg);
   //     }
   //     return map;
   // }
}
