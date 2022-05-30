package com.kiteclub.weather.module.arduino.model;

import java.util.Optional;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.primitives.Ints;
import lombok.Data;

/**
 * New Data format:
 * {"rt":0,"t":24.22,"h":43.47,"p":101743.35,"l":0,"ws":0.00,"wa":8,"gs":0.00,"ga":9,"rmm":0.00,"ms":600053}
 *
 *
 * https://wiki.dfrobot.com/Weather_Station_with_Anemometer_Wind_vane_Rain_bucket_SKU_SEN0186
 * c000s000g000t086r000p000h53b10020
 * c180s003g006t063r000p000h88b10038
 * It outputs 37 bytes per second, including the end CR/LF.
 * <p>
 * Data Parser:
 * <p>
 * c000：air direction, degree
 * s000：air speed(1 minute), 0.1 miles per hour
 * g000：air speed(5 minutes), 0.1 miles per hour
 * t086：temperature, Fahrenheit
 * r000：rainfall(1 hour), 0.01 inches
 * p000：rainfall(24 hours), 0.01 inches
 * h53：humidity,% (00％= 100)
 * b10020：atmosphere,0.1 hpa
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Weather {

    @JsonProperty("r")
    private String result;

    private double windDirection;
    private double windSpeedOneMinuteMs;
    private double windGustSpeedOneMinuteMs;
    //private double windSpeedFiveMinutesMs;
    private double temperatureCelsius;
    /*private int rainfallOneHourInches;
    private int rainfallOneDayInches;
    private int humidity;
    private int pressureHpa;*/

    public void setWeatherData(WeatherData weatherData) {
        temperatureCelsius = weatherData.getT();
        windDirection = weatherData.getWa();
        windDirection = (windDirection + 275) % 360;
        windSpeedOneMinuteMs = weatherData.getWs();
        windGustSpeedOneMinuteMs = weatherData.getGs();
    }
/*
    public void setResult(String result) {
        this.result = result;
        windDirection = getInt(result.substring(1, 4));
        windSpeedOneMinuteMs = getInt(result.substring(5, 8)) * 2;
        windSpeedFiveMinutesMs = getInt(result.substring(9, 12)) * 2;
        //temperatureFahrenheit = getInt(result.substring(13, 16));
        rainfallOneHourInches = getInt(result.substring(17, 20));
        rainfallOneDayInches = getInt(result.substring(21, 24));
        humidity = getInt(result.substring(25, 27));
        pressureHpa = getInt(result.substring(28, 33));
    }*/

    // "c000s000g000t078r000p000h87b10038*3"
    public boolean isDataValid() {
        /*
        return result.charAt(0) == 'c' &&
                result.charAt(4) == 's' &&
                result.charAt(8) == 'g' &&
                result.charAt(12) == 't';
                //&& result.charAt(16) == 'r' &&
                //result.charAt(20) == 'p' &&
                //result.charAt(24) == 'h' &&
                //result.charAt(27) == 'b';
         */
        return temperatureCelsius != 0.0;
    }

    private int getInt(String input) {
        return Optional.ofNullable(input)
                .map(Ints::tryParse)
                .orElse(0);
    }

    /**
     * convert meter/sec to knots/sec for 1 minute average wind speed
     *
     * @return one minute average speed
     */
    public double getWindSpeedAverageKnot() {
        return windSpeedOneMinuteMs * 1.94384;
    }

    /**
     * convert miles/sec to meter/sec for 1 minute average wind speed
     *
     * @return one minute average speed
     */
    public double getWindSpeedAverageMeters() {
        return windSpeedOneMinuteMs * 0.44704;
    }


    public double getWindSpeedMax() {
        //return windSpeedFiveMinutesMs * 0.8689762;
        return windGustSpeedOneMinuteMs * 1.94384;
    }

    public double getWindSpeedMaxMeters() {
        //return windSpeedFiveMinutesMs * 0.44704;
        return windGustSpeedOneMinuteMs;
    }
/*
    public double getRainfallOneHourInches100() {
        return rainfallOneHourInches / 100;
    }

    public double getRainfallOneDayInches100() {
        return rainfallOneDayInches / 100;
    }
*/

    public double getTemperatureFahrenheit() {
        return (temperatureCelsius * 1.8) + 32;
    }

    /*
    //Rainfall (1 hour)
    public double getRainfallOneHour() {
        return rainfallOneHourInches * 0.254;
    }

    //Rainfall (24 hours)
    public double getRainfallOneDay() {
        return rainfallOneDayInches * 0.254;
    }

    //Barometric Pressure
    public double getBarPressure() {
        return pressureHpa / 10.00;
    }

    //Barometric Pressure inch
    public double getBarPressureInch() {
        return pressureHpa / 338.6;
    }
    */
}