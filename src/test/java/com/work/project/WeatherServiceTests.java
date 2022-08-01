package com.work.project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WeatherServiceTests {
    @Mock
    WeatherRepository weatherRepository;
    @InjectMocks
    WeatherService underTest;

    private Weather weather;

    @BeforeEach
    void setUp() {
        weather = new Weather("01.01.2001, 01:11", 11.1);
    }

    @Test
    void itShouldAddWeatherToDatabase() {
        // given
        given(weatherRepository.save(weather)).willReturn(weather);

        // when
        Weather savedWeather = underTest.addWeather(weather);

        // then
        assertThat(savedWeather).isEqualTo(weather);
    }

    @Test
    void itShouldGiveListOfAllWeather() {
        // given
        Weather weather1 = new Weather("02.02.2022, 22:02", -22.2);
        given(weatherRepository.findAll()).willReturn(List.of(weather, weather1));

        // when
        List<Weather> weatherList = underTest.getAllWeather();

        // then
        assertThat(weatherList).isNotNull();
        assertThat(weatherList.size()).isEqualTo(2);
    }

    @Test
    void whenDbEmpty_itShouldGiveEmptyListOfAllWeather() {
        // given
        given(weatherRepository.findAll()).willReturn(Collections.emptyList());

        // when
        List<Weather> weatherList = underTest.getAllWeather();

        // then
        assertThat(weatherList).isEmpty();
    }

    @Test
    void itShouldGetWeatherById() {
        // given
        given(weatherRepository.findById(1L)).willReturn(Optional.of(weather));

        // when
        Weather savedWeather = underTest.getWeatherById(1L).get();

        // then
        assertThat(savedWeather).isNotNull();
        assertThat(savedWeather).isEqualTo(weather);
    }

    @Test
    void itShouldUpdateExistingWeather() {
        // given
        given(weatherRepository.save(weather)).willReturn(weather);
        var updatedTime = "30.03.2003, 23:23";
        weather.setDateTime(updatedTime);
        var updatedTemp = 33.3;
        weather.setTemperature(updatedTemp);

        // when
        Weather updatedWeather = underTest.updateWeather(weather);

        // then
        assertThat(updatedWeather.getDateTime()).isEqualTo(updatedTime);
        assertThat(updatedWeather.getTemperature()).isEqualTo(updatedTemp);
    }

    @Test
    void itShouldDeleteWeather() {
        // given
        long id = 1L;

        willDoNothing().given(weatherRepository).deleteById(id);

        // when
        underTest.deleteWeather(id);

        // then
        verify(weatherRepository, times(1)).deleteById(id);
    }
}
