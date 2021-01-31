package com.kiteclub.weather;

import java.util.concurrent.Executor;
import com.kiteclub.weather.config.PropertiesConfig;
import com.kiteclub.weather.config.model.AppProperties;
import com.kiteclub.weather.module.arduino.rest.ArduinoResource;
import com.kiteclub.weather.module.arduino.service.ArduinoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Slf4j
@EnableAsync
@SpringBootApplication(scanBasePackageClasses = {PropertiesConfig.class,
        AppProperties.class, ArduinoService.class, ArduinoResource.class},
        exclude = {DataSourceAutoConfiguration.class,
                DataSourceTransactionManagerAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class})
public class WeatherApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(WeatherApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }

    @Bean
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(15);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("Data Push-");
        executor.initialize();
        return executor;
    }
}
