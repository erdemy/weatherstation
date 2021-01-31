package com.kiteclub.weather.config;

import com.kiteclub.weather.config.model.RestTemplateProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Created by Erdem YILMAZ
 */
@Configuration
public class RestConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateProperties restTemplateProperties) {
        return new RestTemplate(clientHttpRequestFactory(restTemplateProperties));
    }

    private ClientHttpRequestFactory clientHttpRequestFactory(
            RestTemplateProperties restTemplateProperties) {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setReadTimeout(restTemplateProperties.getReadTimeout());
        factory.setConnectTimeout(restTemplateProperties.getConnectTimeout());
        factory.setConnectionRequestTimeout(restTemplateProperties.getConnectionRequestTimeout());
        return factory;
    }
}