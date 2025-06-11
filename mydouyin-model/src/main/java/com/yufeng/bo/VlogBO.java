package com.yufeng.bo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


/**
 * @author Lzm
 * @Describe 这个BO类属于视频的业务对象，用来接收前端传来的json对象(这个json封装好了视频的信息)，然后变为一个pojo给存入数据库
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class VlogBO {

    private String id;
    private String vlogerId;
    private String url;
    private String cover;
    private String title;
    private Integer width;
    private Integer height;
    private Integer likeCounts;
    private Integer commentsCounts;
    private Integer isPrivate;
}