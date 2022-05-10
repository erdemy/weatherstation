package com.kiteclub.weather.module.arduino.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiteclub.weather.module.arduino.model.Weather;
import com.kiteclub.weather.module.arduino.model.WeatherData;
import com.kiteclub.weather.module.arduino.service.ArduinoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing the weather data.
 */
@RestController
@RequestMapping("/api")
public class ArduinoResource {
    private final Logger log = LoggerFactory.getLogger(ArduinoResource.class);

    private final ArduinoService arduinoService;

    public ArduinoResource(ArduinoService arduinoService) {
        this.arduinoService = arduinoService;
    }

    /**
     * {@code GET  /data} : get weather data.
     *
     * @param value the raw weather data.
     * @throws RuntimeException {@code 500 (Internal Server Error)} if error.
     */
    @GetMapping(value = "/data", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getWeather(@RequestParam(value = "s") String value) throws InterruptedException {
        Weather weatherData = new Weather();
        weatherData.setResult(value);
        log.debug("Raw data:" + value);
        //System.out.println("Value:" + value);
        arduinoService.sendData(weatherData);
        return ResponseEntity
                .ok().body("ok");
    }

    /**
     * {@code GET  /data} : get weather data.
     *
     * @param value the raw weather data.
     * @throws RuntimeException {@code 500 (Internal Server Error)} if error.
     */
    @PostMapping(value = "/json", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getWeatherData(@RequestBody String value) throws InterruptedException {
        log.debug("Raw data:" + value);
        ObjectMapper objectMapper = new ObjectMapper();
        WeatherData data = null;
        try {
            data = objectMapper.readValue(value, WeatherData.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        if (data == null) {
            return ResponseEntity.ok().body("ok");
        }
        System.out.println("Value:" + data);
        Weather weather = new Weather();
        weather.setWeatherData(data);
        arduinoService.sendData(weather);
        return ResponseEntity
                .ok().body("ok");
    }
}
