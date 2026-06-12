package cn.edu.whut.sept.zuul;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("cn.edu.whut.sept.zuul.game")
@EnableJpaRepositories("cn.edu.whut.sept.zuul.game")
public class ZuulServerApplication {
}
