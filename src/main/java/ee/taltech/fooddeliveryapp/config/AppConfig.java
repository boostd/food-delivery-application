package ee.taltech.fooddeliveryapp.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableJpaRepositories(basePackages = {"ee.taltech.fooddeliveryapp"})
@ComponentScan(basePackages = {"ee.taltech.fooddeliveryapp"})
public class AppConfig {

}
