package ee.taltech.fooddeliveryapp.config;

import ee.taltech.fooddeliveryapp.scheduler.ImportWeatherTask;
import ee.taltech.fooddeliveryapp.scheduler.Scheduler;
import ee.taltech.fooddeliveryapp.service.DeliveryFeeCalculator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableJpaRepositories(basePackages = {"ee.taltech.fooddeliveryapp.database"})
@ComponentScan(basePackages = {"ee.taltech.fooddeliveryapp"})
public class AppConfig {

    @Bean
    public ImportWeatherTask importWeatherTask() {
        return new ImportWeatherTask();
    }

    @Bean
    @ConditionalOnProperty(name = "fooddeliveryapp.enableScheduler", havingValue = "true", matchIfMissing = true)
    public Scheduler scheduler() {
        return new Scheduler();
    }

    @Bean
    public DeliveryFeeCalculator deliveryFeeCalculator() {
        return new DeliveryFeeCalculator();
    }

}
