package com.kiteclub.weather.service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import com.kiteclub.weather.module.arduino.service.ArduinoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SchedularService {
    private ArduinoService arduinoService;

    public SchedularService(ArduinoService arduinoService) {
        this.arduinoService = arduinoService;
    }

    //@Scheduled(cron = "0 * * * * *")
    //@Scheduled(cron = "*/10 * * * * *")
    public void reportCurrentTime() {
        // current date in UTC+3, no matter what the JVM default timezone is
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.ofHours(3)).truncatedTo(ChronoUnit.SECONDS);
        arduinoService.getData(zonedDateTime);
    }

}
