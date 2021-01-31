package com.kiteclub.weather.config.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by Erdem YILMAZ
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RestTemplateProperties {

    private int connectionRequestTimeout;

    private int connectTimeout;

    private int readTimeout;

}
