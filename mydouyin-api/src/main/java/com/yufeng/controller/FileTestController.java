package com.yufeng.controller;

import com.yufeng.config.MinIOConfig;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Lzm
 * @CreateTime 2025年5月05日 18:05
 */

@Slf4j
@Api(tags = "FileController 文件上传测试的接口")
@RestController
public class FileTestController {

    // 注入minioClient这个全局的bean
    @Autowired
    private MinIOConfig minioClient;


    /**
     * MultipartFile 的主要用途
     * 处理文件上传：它封装了客户端上传的文件数据，使服务器端可以轻松访问和处理
     * 获取文件属性：提供方法获取文件名、内容类型、大小等属性
     * 访问文件内容：可以获取文件的字节数组或输入流
     * 常用方法
     * String getOriginalFilename()：获取原始文件名
     * String getContentType()：获取文件的内容类型
     * boolean isEmpty()：判断文件是否为空
     * long getSize()：获取文件大小
     * byte[] getBytes()：获取文件内容的字节数组
     * InputStream getInputStream()：获取文件内容的输入流
     * transferTo(File dest)：将上传文件转存到服务器上指定的文件
     */
    // @PostMapping("/upload")
    // public GraceJSONResult upload(MultipartFile file) throws Exception {
    //     log.info("upload接口被调用了");
    //     String fileName = file.getOriginalFilename();
    //
    //     // 通过流式上传文件到minio
    //     MinIOUtils.uploadFile(minioClient.getBucketName(),
    //             fileName,
    //             file.getInputStream());
    //
    //     // 返还一个文件的访问路径
    //     String url =  minioClient.getFileHost()
    //                    + "/" + minioClient.getBucketName()
    //                    + "/" + fileName;
    //
    //     return GraceJSONResult.ok(url);
    // }
}
