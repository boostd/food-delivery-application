package ee.taltech.fooddeliveryapp.config;

import ee.taltech.fooddeliveryapp.database.WeatherDataRepository;
import ee.taltech.fooddeliveryapp.scheduler.ImportWeatherTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class AppConfig {
    @Autowired
    WeatherDataRepository weatherDataRepository;

    @Bean
    public ImportWeatherTask importWeatherTask() {
        return new ImportWeatherTask();
    }
}
