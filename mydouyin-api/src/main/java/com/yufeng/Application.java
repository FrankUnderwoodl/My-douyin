package com.yufeng;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import tk.mybatis.spring.annotation.MapperScan;


/**
 * @author Lzm
 * @CreateTime 2025年5月05日 18:32
 * -----------------------------------------------
 * 对于 @SpringBootApplication 注解的再次理解：
 * 1. Spring Boot 会自动扫描 com.yufeng 包及其所有子包
 * 2. 它会自动注册这些包中的所有组件（如标记了 @Component、@Service、@Repository、@Controller 等注解的类）
 * 这就是为什么你的项目中的服务实现类 UserServiceImpl（在 com.yufeng.service.impl 包中）能够被自动发现并注册为 Spring Bean。
 * 然而，值得注意的是，你还单独使用了 @MapperScan(basePackages = {"com.yufeng.mapper"}) 注解来明确指定 MyBatis 映射器接口的位置，这是因为 MyBatis 的映射器接口需要特殊处理，不能仅依靠默认的组件扫描机制来注册。
 * -------------------------------------------------
 * 为什么MyBatis 的映射器接口需要特殊处理，不能仅依靠默认的组件扫描机制来注册？
 * 因为 ①接口实现机制不同：
 * 普通的 Spring 组件（如 @Service、@Component 等）都是具体的类实现
 * 而 MyBatis 的映射器是接口，没有具体实现类，需要由 MyBatis 在运行时动态生成代理实现
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.yufeng", "org.n3r.idworker"})
@MapperScan(basePackages = {"com.yufeng.mapper"})
@EnableMongoRepositories
public class Application {
    public static void main(String[] args) {
        // 1. 启动 Spring Boot 应用
        SpringApplication.run(Application.class, args);
    }
}
