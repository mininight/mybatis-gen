package module.level.two;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Module two`s starter
 *
 * @author Ivan
 * @version 1.0.0
 * @date 2021-06-13 13:32
 */
@MapperScan
@SpringBootApplication
public class ModuleTwoStarter {
    public static void main(String[] args) {
        SpringApplication.run(ModuleTwoStarter.class, args);
    }
}
