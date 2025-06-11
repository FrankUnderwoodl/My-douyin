package com.yufeng.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Lzm
 * @Describe 这个View Object具体来说，就是用来封装一个短视频的，例如视频的id、作者id、视频封面、视频描述、点赞数、评论数、分享数、是否点赞等
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class IndexVlogVO {
    private String vlogId; // 视频id
    private String vlogerId; // 作者id
    private String vlogerFace; // 作者头像
    private String vlogerName; // 作者昵称
    private String content; // 视频描述
    private String url; // 视频地址
    private String cover; // 封面地址
    private Integer width; // 视频宽度
    private Integer height; // 视频高度
    private Integer likeCounts; // 点赞数
    private Integer commentsCounts; // 评论数
    private Integer isPrivate; // 是否私密
    // 视频是否正在播放
    private boolean isPlay = false;
    // 我是否有关注上传者
    private boolean doIFollowVloger = false;
    // 我是否有点赞该视频
    private boolean doILikeThisVlog = false;
}