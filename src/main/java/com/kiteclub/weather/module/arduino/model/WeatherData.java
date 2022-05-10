package com.kiteclub.weather.module.arduino.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/*
 * New Data format:
 * {"rt":0,"t":24.22,"h":43.47,"p":101743.35,"l":0,"ws":0.00,"wa":8,"gs":0.00,"ga":9,"rmm":0.00,"ms":600053}
 *
 * rt: hardcoded for 1 minute data
 * t: temperature
 * h: humidity
 * p: pressure
 * l: lux
 * ws: wind speed
 * wa: wind angle
 * gs: gust speed
 * ga: gust angle
 * rmm: rain
 * ms: iteration number
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherData {
    private double rt;
    private double t;
    private double h;
    private double p;
    private double l;
    private double ws;
    private double wa;
    private double gs;
    private double ga;
    private double rmm;
    private double ms;
}