package com.kiteclub.weather.config;

import com.kiteclub.weather.config.model.AppProperties;
import com.kiteclub.weather.config.model.RestTemplateProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class PropertiesConfig {

    private final AppProperties appProperties;

    public PropertiesConfig(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Bean
    public RestTemplateProperties restProperties() {
        return appProperties.getRest();
    }

}
