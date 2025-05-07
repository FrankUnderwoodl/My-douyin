package com.yufeng.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @author Lzm
 */
@Component
@Data
@PropertySource("classpath:tencentcloud.properties")
@ConfigurationProperties(prefix = "tencent.cloud") // 这个前缀会自动映射到类的属性上
public class TencentCloudProperties {


    private String secretId;
    private String secretKey;

}
