package com.kiteclub.weather.module.arduino.service;

import static org.springframework.http.HttpMethod.GET;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.kiteclub.weather.module.arduino.model.Weather;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Created by Erdem Yilmaz.
 */
@Component
@Slf4j
public class ArduinoService {
    @Value("${app.arduino.server}")
    public String ARDUINO_SERVER;
    // ************ Windguru variables *********
    // Create Instance of HashFunction (MD5)
    private static final HashFunction hashFunction = Hashing.md5();

    @Value("${app.windguru.url}")
    private String WINDGURU_URL ;
    @Value("${app.windguru.stationId}")
    private String STATION_ID;
    @Value("${app.windguru.password}")
    private String STATION_PASSWORD;


    // ************ Windy variables *********
    @Value("${app.windy.url}")
    private String WINDY_BASE_URL;
    @Value("${app.windy.apiKey}")
    private String WINDY_API_KEY;

    // ************ Windyapp variables *********
    @Value("${app.windyapp.url}")
    private String WINDY_APP_URL;
    @Value("${app.windyapp.secret}")
    private String WINDY_APP_SECRET;

    // ************ pwsweather.com variables *********
    @Value("${app.pswWeather.url}")
    private String PWSWEATHER_URL;
    @Value("${app.pswWeather.stationId}")
    private String PWSWEATHER_STATION_ID;
    @Value("${app.pswWeather.apiKey}")
    private String PWSWEATHER_API_KEY;

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd+HH:mm:ss");

    // TODO: faster string validation and split
    // Pattern pattern = Pattern.compile("c(\\d{3})s(\\d{3})g(\\d{3})t(\\d{3})r(\\d{3})p(\\d{3})h(\\d{2})b(\\d{5})");



    private RestTemplate restTemplate;

    public ArduinoService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void getData(ZonedDateTime time) {
        try {
            //Weather data = restTemplate.getForObject(ARDUINO_SERVER, Weather.class);
            Weather data = new Weather();
            data.setResult("c180s003g006t063r000p000h88b10038*3");

            //System.out.println("Data:" + data.getResult());
            if (data.isDataValid()) {
                /*System.out.println("getWindDirection:" + data.getWindDirection());
                System.out.println("getWindSpeedAverage:" + data.getWindSpeedAverage());
                System.out.println("getWindSpeedMax:" + data.getWindSpeedMax());
                System.out.println("getTemperature:" + data.getTemperature());
                System.out.println("getRainfallOneHour:" + data.getRainfallOneHour());
                System.out.println("getRainfallOneDay:" + data.getRainfallOneDay());
                System.out.println("getHumidity:" + data.getHumidity());
                System.out.println("getBarPressure:" + data.getBarPressure());*/
                long salt = time.toInstant().toEpochMilli();
                System.out.println(salt + "," + data.getResult());

                sendDataToWindGuru(data, time);
                sendDataToWindy(data, time);
                sendDataToWindyApp(data, time);
                sendDataToPswWeather(data, time);
            }
            log.debug("Time {}, Result value {}", time, data.getResult());
        } catch (Exception exp) {
            log.error("ArduinoService error", exp);
        }
    }

    /**
     * https://stations.windguru.cz/upload_api.php
     * GET variables to send:
     * <p>
     * uid	(required)	UID of your station = unique string you choosed during station registration
     * interval		measurement interval in seconds (60 would mean you are sending 1 minute measurements), then the
     * wind_avg / wind_max / wind_min values should be values valid for this past interval
     * wind_avg		average wind speed during interval (knots)
     * wind_max		maximum wind speed during interval (knots)
     * wind_min		minimum wind speed during interval (knots)
     * wind_direction		wind direction as degrees (0 = north, 90 east etc...)
     * temperature		temperature (celsius)
     * rh		relative humidity (%)
     * mslp		pressure reduced to sea level (hPa)
     * precip		precipitation in milimeters (not displayed anywhere yet, but API is ready to accept)
     * precip_interval		interval for the precip value in seconds (if not set then 3600 = 1 hour is assumed)
     *
     * @param data weather data
     * @param time time info
     */
    public void sendDataToWindGuru(Weather data, ZonedDateTime time) {
        try {
            long salt = time.toInstant().toEpochMilli();

            // Pass input and charset to hashString() method
            HashCode hash = hashFunction.hashString(salt + STATION_ID + STATION_PASSWORD, StandardCharsets.UTF_8);
            //hash.toString();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(WINDGURU_URL)
                    .queryParam("uid", STATION_ID)
                    .queryParam("salt", salt)
                    .queryParam("hash", hash.toString())
                    .queryParam("interval", 60)
                    .queryParam("wind_avg", data.getWindSpeedAverage())
                    .queryParam("wind_max", data.getWindSpeedMax())
                    .queryParam("wind_direction", data.getWindDirection())
                    .queryParam("temperature", data.getTemperatureCelsius())
                    .queryParam("rh", data.getHumidity())
                    .queryParam("mslp", data.getBarPressure())
                    .queryParam("precip", data.getRainfallOneHour())
                    .queryParam("precip_interval", 3600);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            HttpEntity<String> response = restTemplate.exchange(
                    builder.toUriString(),
                    GET,
                    entity,
                    String.class);
        } catch (Exception exception) {
            log.error("sendDataToWindGuru send error", exception);
        }

    }

    /**
     * https://community.windy.com/topic/8168/report-your-weather-station-data-to-windy/2?lang=en-GB
     * Info Part Parameters
     * <p>
     * Station record in the database is created as soon as required info params are uploaded (station, lat, lon).
     * <p>
     * station - 32 bit integer; required for multiple stations; default value 0; alternative names: si, stationId
     * shareOption - text one of: Open, Only Windy, Private; default value is Open
     * name - text; user selected station name
     * latitude - number [degrees]; required; north–south position on the Earth`s surface
     * longitude - number [degrees]; required; east–west position on the Earth`s surface
     * elevation - number [metres]; height above the Earth's sea level (reference geoid); alternative names: elev,
     * elev_m, altitude
     * tempheight - number [metres]; temperature sensor height above the surface; alternative names: agl_temp
     * windheight - number [metres]; wind sensors height above the surface; alternative names: agl_wind
     * <p>
     * Measurements
     * <p>
     * station - 32 bit integer; required for multiple stations; default value 0; alternative names: si, stationId
     * time - text; iso string formated time "2011-10-05T14:48:00.000Z"; when time (or alternative) is NOT present
     * server time is used
     * dateutc - text; UTC time formated as "2001-01-01 10:32:35"; (alternative to time)
     * ts - unix timestamp [s] or [ms]; (alternative to time)
     * temp - real number [°C]; air temperature
     * tempf - real number [°F]; air temperature (alternative to temp)
     * wind - real number [m/s]; wind speed
     * windspeedmph - real number [mph]; wind speed (alternative to wind)
     * winddir - integer number [deg]; instantaneous wind direction
     * gust - real number [m/s]; current wind gust
     * windgustmph - real number [mph]; current wind gust (alternative to gust)
     * rh - real number [%]; relative humidity ; alternative name: humidity
     * dewpoint - real number [°C];
     * pressure - real number [Pa]; atmospheric pressure
     * mbar - real number [milibar, hPa]; atmospheric pressure alternative
     * baromin - real number [inches Hg]; atmospheric pressure alternative
     * precip - real number [mm]; precipitation over the past hour
     * rainin - real number [in]; rain inches over the past hour (alternative to precip)
     * uv - number [index];
     *
     * @param data weather data
     * @param time time info
     */
    public void sendDataToWindy(Weather data, ZonedDateTime time) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(WINDY_BASE_URL + WINDY_API_KEY)
                    .queryParam("tempf", data.getTemperatureFahrenheit())
                    .queryParam("wind", data.getWindSpeedOneMinuteMs())
                    .queryParam("winddir", data.getWindDirection())
                    .queryParam("gust", data.getWindSpeedFiveMinutesMs())
                    .queryParam("rh", data.getHumidity())
                    .queryParam("mbar", data.getBarPressure())
                    .queryParam("rainin", data.getRainfallOneHourInches());
            HttpEntity<?> entity = new HttpEntity<>(headers);

            HttpEntity<String> response = restTemplate.exchange(
                    builder.toUriString(),
                    GET,
                    entity,
                    String.class);
        } catch (Exception exception) {
            log.error("sendDataToWindy send error", exception);
        }

    }

    /**
     * https://windyapp.co/apiV9.php?method=addCustomMeteostation&secret=456HJHlfcyg89&d5=123&a=11&m=10&g=15&p=200
     * &te2=20&i=test1
     * //d5* - direction from 0 to 1024. direction in degrees is equal = (d5/1024)*360
     * //accum - external potential. should be divided by 10 to convert into voltage
     * //p- pressure. for mmHg should be divided by 1,33. For kPa - divide by 10 (this sensor is not installed in 0099)
     * //thc - temperature of internal pressure sensor (this sensor is not installed in 0099)
     * //te2 - température of the external temperature sensor
     * //a* - average wind per sending interval. for m/c - divide by 10
     * //m* - minimal wind per sending interval. for m/c - divide by 10
     * //g* - maximum wind per sending interval. for m/c - divide by 10
     * //i* - device number
     * //b - internal tension. for volts - divide by 100
     * //h - humidity, if sensor is installed
     * //secret=456HJHlfcyg89
     * <p>
     * *required fields
     * i - what ever you like just tell us what is the name of this station(visible for users of windy.app) and lat/lng
     * <p>
     * A few notes:
     * -we’ll need the station id, station name and the lat/lon coordinates
     * - we need the wind in m/s, temp is C and pressure in hpa.
     * - we don’t need the data from the station more often than every minute
     *
     * @param data weather data
     * @param time time info
     */
    public void sendDataToWindyApp(Weather data, ZonedDateTime time) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(WINDY_APP_URL)
                    .queryParam("method", "addCustomMeteostation")
                    .queryParam("secret", WINDY_APP_SECRET)
                    .queryParam("d5", data.getWindDirection() * 2.84444)
                    .queryParam("a", data.getWindSpeedOneMinuteMs())
                    .queryParam("m", data.getWindSpeedOneMinuteMs())
                    .queryParam("g", data.getWindSpeedFiveMinutesMs())
                    .queryParam("p", data.getBarPressure())
                    .queryParam("te2", data.getTemperatureCelsius())
                    .queryParam("h", data.getHumidity())
                    .queryParam("i", "81KiteClub");
            HttpEntity<?> entity = new HttpEntity<>(headers);

            HttpEntity<String> response = restTemplate.exchange(
                    builder.toUriString(),
                    GET,
                    entity,
                    String.class);
        } catch (Exception exception) {
            log.error("sendDataToWindyApp send error", exception);
        }
    }

    /**
     * SAMPLE STRING TO UPDATE DATA ON PWSWEATHER.com
     *
     *
     * https://pwsupdate.pwsweather.com/api/v1/submitwx?ID=STATIONID&PASSWORD=APIkey&dateutc=2000-12-01+15:20:01
     * &winddir=225&windspeedmph=0.0&windgustmph=0.0&tempf=34.88&rainin=0.06&dailyrainin=0.06&monthrainin=1
     * .02&yearrainin=18.26&baromin=29.49&dewptf=30.16&humidity=83&weather=OVC&solarradiation=183&UV=5
     * .28&softwaretype=Examplever1.1&action=updateraw
     *
     *
     * All parameters are optional except for the ones marked with *.
     * If your software or hardware doesn't support a parameter it can be omitted from the string.
     *
     *
     * ID *		Station ID as registered
     * PASSWORD *	The API key available on the station's page
     * dateutc	*	Date and time in the format of year-mo-da+hour:min:sec
     * winddir		Wind direction in degrees
     * windspeedmph	Wind speed in miles per hour
     * windgustmph	Wind gust in miles per hour
     * tempf		Temperature in degrees fahrenheit
     * rainin		Hourly rain in inches
     * dailyrainin	Daily rain in inches
     * monthrainin	Monthly rain in inches
     * yearrainin	Seasonal rain in inches (usually local meteorological year)
     * baromin		Barometric pressure in inches
     * dewptf		Dew point in degrees fahrenheit
     * humidity	Humidity in percent
     * weather		Current weather or sky conditions using standard METAR abbreviations and intensity (e.g. -RA, +SN,
     * SKC, etc.)
     * solarradiation	Solar radiation
     * UV		UV
     * softwaretype *	Software type
     *
     *
     * The string always concludes with action=updateraw to indicate the end of the readings
     * @param data weather data
     * @param time time info
     */
    public void sendDataToPswWeather(Weather data, ZonedDateTime time) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(PWSWEATHER_URL)
                    .queryParam("ID", PWSWEATHER_STATION_ID)
                    .queryParam("PASSWORD", PWSWEATHER_API_KEY)
                    .queryParam("dateutc", time.format(formatter))
                    .queryParam("winddir", data.getWindDirection())
                    .queryParam("windspeedmph", data.getWindSpeedOneMinuteMs())
                    .queryParam("windgustmph", data.getWindSpeedFiveMinutesMs())
                    .queryParam("tempf", data.getTemperatureFahrenheit())
                    .queryParam("rainin", data.getRainfallOneHourInches())
                    .queryParam("dailyrainin", data.getRainfallOneDayInches())
                    .queryParam("baromin", data.getBarPressure())
                    .queryParam("humidity", data.getHumidity())
                    .queryParam("softwaretype", "version1.0")
                    .queryParam("action", "updateraw");
            HttpEntity<?> entity = new HttpEntity<>(headers);

            HttpEntity<String> response = restTemplate.exchange(
                    builder.toUriString(),
                    GET,
                    entity,
                    String.class);
        } catch (Exception exception) {
            log.error("sendDataToPswWeather send error", exception);
        }
    }
}
