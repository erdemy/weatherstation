package com.kiteclub.weather;

import java.util.concurrent.Executor;
import com.kiteclub.weather.config.PropertiesConfig;
import com.kiteclub.weather.config.model.AppProperties;
import com.kiteclub.weather.module.arduino.service.ArduinoService;
import com.kiteclub.weather.service.SchedularService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableScheduling
@Slf4j
@EnableAsync
@ServletComponentScan(basePackages = {"com.kiteclub.weather.module.arduino.servlet"})
@SpringBootApplication(scanBasePackageClasses = {PropertiesConfig.class,
        AppProperties.class, SchedularService.class, ArduinoService.class},
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
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("Data Lookup-");
        executor.initialize();
        return executor;
    }
}
