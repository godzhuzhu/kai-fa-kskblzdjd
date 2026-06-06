package cn.edu.whut.sept.zuul;

import org.springframework.boot.SpringApplication;

/**
 * World of Zuul 服务启动器。
 * <p>
 * 通过 {@link SpringApplication#run(Class, String...)} 引导
 * {@link ZuulServerApplication} 所配置的 Spring Boot 应用上下文，
 * 并启动嵌入式 Web 服务器。
 * </p>
 *
 * <p>启动方式：{@code mvn spring-boot:run} 或直接执行打包后的 jar。</p>
 *
 * @author sept
 * @since 1.0
 */
public class ServerMain {

    public static void main(String[] args) {
        SpringApplication.run(ZuulServerApplication.class, args);
    }
}
