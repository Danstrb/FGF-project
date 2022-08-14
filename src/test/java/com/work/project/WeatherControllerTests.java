package com.work.project;

import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
public class WeatherControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherService weatherService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void itShouldCreateWeather() throws Exception {
        // given
        Weather weather = new Weather("01.01.2001, 01:11", 11.1);
        given(weatherService.addWeather(any(Weather.class))).willAnswer((i) -> i.getArgument(0));

        // when
        ResultActions response = mockMvc.perform(post("/api/v1/weathers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(weather)));

        // then
        response.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dateTime", is(weather.getDateTime())))
                .andExpect(jsonPath("$.temperature", is(weather.getTemperature())));
    }

    @Test
    public void itShouldGetAllWeather() throws Exception {
        // given
        List<Weather> weatherList = new ArrayList<>();
        weatherList.add(new Weather("11.01.2001, 11:11", 11.1));
        weatherList.add(new Weather("22.02.2002, 22:22", -22.2));
        given(weatherService.getAllWeather()).willReturn(weatherList);

        // when
        ResultActions response = mockMvc.perform(get("/api/v1/weathers"));

        // then
        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.size()", is(weatherList.size())));
    }

    @Test
    public void givenValidId_shouldGetWeatherById() throws Exception {
        // given
        Long id = 1L;
        Weather weather = new Weather(id, "11.01.2001, 11:11", 11.1);
        given(weatherService.getWeatherById(id)).willReturn(Optional.of(weather));

        // when
        ResultActions response = mockMvc.perform(get("/api/v1/weathers/{id}", id));

        // then
        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.dateTime", is(weather.getDateTime())))
                .andExpect(jsonPath("$.temperature", is(weather.getTemperature())));
    }

    @Test
    public void givenInvalidId_shouldGetWeatherById() throws Exception {
        // given
        Long validId = 1L;
        Long invalidId = 2L;
        Weather weather = new Weather(validId, "11.01.2001, 11:11", 11.1);
        given(weatherService.getWeatherById(invalidId)).willReturn(Optional.empty());

        // when
        ResultActions response = mockMvc.perform(get("/api/v1/weathers/{id})", invalidId));

        // then
        response.andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    public void shouldUpdateWeather() throws Exception {
        // given
        Long id = 1L;
        var originalWeather = new Weather(id,"11.01.2001, 11:11", 11.1);
        var updatedWeather = new Weather (id,"22.02.2002, 22:22", -22.2);

        given(weatherService.getWeatherById(id)).willReturn(Optional.of(originalWeather));
        given(weatherService.updateWeather(any(Weather.class))).willAnswer((w) -> w.getArgument(0));

        // when
        ResultActions response = mockMvc.perform(put("/api/v1/weathers/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedWeather)));

        // then
        response.andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void givenValidId_shouldDeleteWeather() throws Exception {
        // given
        Long id = 1L;
        willDoNothing().given(weatherService).deleteWeather(id);

        // when
        ResultActions response = mockMvc.perform(delete("/api/v1/weathers/{id}", id));

        // then
        response.andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void givenInvalidId_shouldDeleteWeather() throws Exception {
        // given
        Long validId = 1L;
        Long invalidId = 2L;
        willDoNothing().given(weatherService).deleteWeather(invalidId);

        // when
        ResultActions response = mockMvc.perform(delete("/api/v1/weathers/{id}", invalidId));

        // then
        response.andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void itShouldGetWeatherInTemperatureRange() throws Exception {
        // given
        double lowerTempLimit = 12.1;
        double upperTempLimit = 15.1;
        List<Weather> weatherList = List.of(
                new Weather(1L, "01.08.2001, 09:00", 11.5),
                new Weather(2L, "02.08.2001, 10:00", 12.5),
                new Weather(3L, "03.08.2001, 11:00", 13.5),
                new Weather(4L, "04.08.2001, 12:00", 14.5),
                new Weather(5L, "05.08.2001, 13:00", 15.5));
        List<String> resultList = List.of("02.08.2001, 10:00 to 04.08.2001, 12:00");
        given(weatherService.findLongestTempRange(lowerTempLimit, upperTempLimit)).willReturn(resultList);

        // when
        // then
        mockMvc.perform(get("/api/v1/weathers/temprange")
                .param("lowerTempLimit", "12.1")
                .param("upperTempLimit", "15.1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(resultList)));
    }

    @Test
    public void itShouldGetWeatherInTemperatureRangeAtTime() throws Exception {
        // given
        double lowerTempLimit = 12.1;
        double upperTempLimit = 15.1;
        String lowerHourLimit = "09:30";
        String upperHourLimit = "12:30";
        List<Weather> weatherList = List.of(
                new Weather(1L, "01.08.2001, 09:00", 11.5),
                new Weather(2L, "02.08.2001, 10:00", 12.5),
                new Weather(3L, "03.08.2001, 11:00", 13.5),
                new Weather(4L, "04.08.2001, 12:00", 14.5),
                new Weather(5L, "05.08.2001, 13:00", 15.5));
        List<String> resultList = List.of("02.08.2001, 10:00 to 04.08.2001, 12:00");
        given(weatherService.findLongestTempRangeAtTime(lowerTempLimit, upperTempLimit, lowerHourLimit, upperHourLimit)).willReturn(resultList);

        // when
        // then
        mockMvc.perform(get("/api/v1/weathers/temprange/timelimit")
                        .param("lowerTempLimit", Double.toString(lowerTempLimit))
                        .param("upperTempLimit", Double.toString(upperTempLimit))
                        .param("lowerTimeLimit", lowerHourLimit)
                        .param("upperTimeLimit", upperHourLimit))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(resultList)));
    }
}
