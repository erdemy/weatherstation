package com.kiteclub.weather.config.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("app")
public class AppProperties {

    private RestTemplateProperties rest;

}
