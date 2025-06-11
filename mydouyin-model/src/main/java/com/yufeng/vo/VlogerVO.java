package com.yufeng.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class VlogerVO {
    private String vlogerId;
    private String nickname;
    private String face;
    // 默认为true(因为是我关注的博主)
    private boolean isFollowed = true;
}