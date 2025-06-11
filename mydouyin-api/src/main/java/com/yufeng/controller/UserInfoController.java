package com.yufeng.controller;

import com.yufeng.base.BaseInfoProperties;
import com.yufeng.bo.UpdatedUserBO;
import com.yufeng.config.MinIOConfig;
import com.yufeng.enums.FileTypeEnum;
import com.yufeng.enums.UserInfoModifyType;
import com.yufeng.grace.result.GraceJSONResult;
import com.yufeng.grace.result.ResponseStatusEnum;
import com.yufeng.pojo.Users;
import com.yufeng.service.UserService;
import com.yufeng.utils.MinIOUtils;
import com.yufeng.vo.UsersVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


/**
 * @author Lzm
 * @CreateTime 2025年5月05日 18:05
 */
@Slf4j
@RestController
@Api(tags = "UserInfoController 用户信息接口模块") // 这个注解可以用来定义接口类的名字
@RequestMapping("/userInfo")
public class UserInfoController extends BaseInfoProperties {

    @Autowired
    private UserService userService;

    // 注入minioClient这个全局的bean，用于上传头像、背景图
    @Autowired
    private MinIOConfig minioClient;

    /**
     * 这个接口是用来查询用户信息的，前端需传入userId
     * 主要用于，每当主页刷新的时候，就能自动展示你的新粉丝、新名称(如果你更改的话)、也用作你进入某个人的主页时,也会自动调用这个接口
     */
    @GetMapping("/query")
    public Object query(@RequestParam String userId) {
        log.info("查询用户信息的ID是：{}", userId);

        Users user = this.userService.getUser(userId);
        // 因为前端需要的是一个vo对象(比pojo有更多的属性，比如关注了哪些博主、自己的粉丝，但是这些属性放在了Redis中)，所以我们需要将user对象转换成vo对象
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(user, usersVO);

        // 用户关注博主总数量
        String myFollowsCountsStr = redis.get(REDIS_MY_FOLLOWS_COUNTS + ":" + userId);
        // 用户粉丝总数
        String myFansCountsStr = redis.get(REDIS_MY_FANS_COUNTS + ":" + userId);
        // 用户获赞总数，视频博主（点赞/喜欢）总和
        // String likedVlogCountsStr = redis.get(REDIS_VLOG_BE_LIKED_COUNTS + ":" + userId);
        String likedVlogerCountsStr = redis.get(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + userId);

        // 设置数据(初始化)
        Integer myFollowsCounts = 0;
        Integer myFansCounts = 0;
        Integer likedVlogCounts = 0;
        Integer likedVlogerCounts = 0;
        Integer totalLikeMeCounts = 0;

        // 因为在Redis中获取到的值是String类型的，所以需要转换成int类型
        if (StringUtils.isNotBlank(myFollowsCountsStr)) {
            myFollowsCounts = Integer.valueOf(myFollowsCountsStr);
        }
        if (StringUtils.isNotBlank(myFansCountsStr)) {
            myFansCounts = Integer.valueOf(myFansCountsStr);
        }
       // if (StringUtils.isNotBlank(likedVlogCountsStr)) {
       //     likedVlogCounts = Integer.valueOf(likedVlogCountsStr);
       // }
        if (StringUtils.isNotBlank(likedVlogerCountsStr)) {
            likedVlogerCounts = Integer.valueOf(likedVlogerCountsStr);
        }
        totalLikeMeCounts = likedVlogCounts + likedVlogerCounts;

        usersVO.setMyFollowsCounts(myFollowsCounts);
        usersVO.setMyFansCounts(myFansCounts);
        usersVO.setTotalLikeMeCounts(totalLikeMeCounts);

        return GraceJSONResult.ok(usersVO);
    }


    // 这个接口对应前端类似这样的接口，url: serverUrl + "/userInfo/modifyUserInfo?type=1",一个接口能实现6种效果呢
    @PostMapping("modifyUserInfo")
    public GraceJSONResult modifyUserInfo(@RequestBody UpdatedUserBO updatedUserBO, @RequestParam Integer type) {

        log.info("修改用户信息的参数是：{}", updatedUserBO.toString());
        // 先判断前端传来的type类型是否正确，如果不正确就抛出异常，后面就不执行了
        UserInfoModifyType.checkUserInfoTypeIsRight(type);

        // 直接调用service层的修改用户信息接口
        Users newUserInfo = userService.UpdateUserInfo(updatedUserBO, type);

        // 把新的用户信息返回给前端
        return GraceJSONResult.ok(newUserInfo);
    }



    @PostMapping("/modifyImage")
    public GraceJSONResult modifyImage(@RequestParam String userId,
                                  @RequestParam Integer type,
                                  MultipartFile file) throws Exception {
        log.info("upload接口被调用了，用户开始修改头像/背景图了，参数是：userId={}, type={}, file={}", userId, type, file);

        // 1.首先判断一下用户传过来的type类型是否正确
        if(type != FileTypeEnum.BGIMG.type && type != FileTypeEnum.FACE.type) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }

        // 2.将用户传过来的文件上传到minio，并且拿到url并存入数据库中
        String fileName = file.getOriginalFilename(); // 获取文件名
        // 通过流式上传文件到minio
        MinIOUtils.uploadFile(minioClient.getBucketName(),
                fileName,
                file.getInputStream());

        // 返还访问这个文件的访问路径
        String url =  minioClient.getFileHost()
                + "/" + minioClient.getBucketName()
                + "/" + fileName;

        // 3.将文件的url存入数据库，这里可以调用之前实现过的updateUserInfo接口
        UpdatedUserBO updatedUserBO = new UpdatedUserBO();
        updatedUserBO.setId(userId);
        if (type == FileTypeEnum.BGIMG.type) {
            // 说明是背景图
            updatedUserBO.setBgImg(url);

        } else if (type == FileTypeEnum.FACE.type) {
            // 说明是头像
            updatedUserBO.setFace(url);
        }

        Users user = userService.UpdateUserInfo(updatedUserBO);

        return GraceJSONResult.ok(user);
    }
}
