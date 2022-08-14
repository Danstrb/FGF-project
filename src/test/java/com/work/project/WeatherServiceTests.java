package com.work.project;

import com.work.project.exceptions.EmptyRepositoryException;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    void whenDbEmpty_whenGettingAllWeather_itShouldGiveEmptyListOfAllWeather() {
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

    @Test
    void whenDbEmpty_whenGettingLongestTemperatureRange_itShouldThrowEmptyRepositoryException () {
        // given
        double lowerTempLimit = 12.1;
        double upperTempLimit = 15.1;
        List<Weather> weatherList = List.of();

        given(weatherRepository.findAll()).willReturn(weatherList);

        // when
        EmptyRepositoryException expectedException = assertThrows(EmptyRepositoryException.class, () -> underTest.findLongestTempRange(lowerTempLimit, upperTempLimit));

        // then
        assertTrue(expectedException.getMessage().contains("empty"));
        assertThat(expectedException.getMessage()).isEqualTo("The database is empty.");
    }

    @Test
    void whenDbHasOneEntryAndIsInRange_whenGettingLongestTemperatureRange_itShouldReturnOneEntry () throws EmptyRepositoryException {
        // given
        double lowerTempLimit = 12.1;
        double upperTempLimit = 15.1;
        List<Weather> weatherList = List.of(
                new Weather(3L, "03.08.2001, 11:00", 13.5));
        List<String> resultList = List.of("03.08.2001, 11:00");

        given(weatherRepository.findAll()).willReturn(weatherList);

        // when
        List<String> testedResultList = underTest.findLongestTempRange(lowerTempLimit, upperTempLimit);

        // then
        assertThat(testedResultList).isEqualTo(resultList);
    }

    @Test
    void whenDbHasOneEntryAndIsEqualToLowerLimit_whenGettingLongestTemperatureRange_itShouldReturnOneEntry () throws EmptyRepositoryException {
        // given
        double lowerTempLimit = 12.1;
        double upperTempLimit = 15.1;
        List<Weather> weatherList = List.of(
                new Weather(3L, "03.08.2001, 11:00", lowerTempLimit));
        List<String> resultList = List.of("03.08.2001, 11:00");

        given(weatherRepository.findAll()).willReturn(weatherList);

        // when
        List<String> testedResultList = underTest.findLongestTempRange(lowerTempLimit, upperTempLimit);

        // then
        assertThat(testedResultList).isEqualTo(resultList);
    }

    @Test
    void whenDbHasOneEntryAndIsEqualToUpperLimit_whenGettingLongestTemperatureRange_itShouldReturnOneEntry () throws EmptyRepositoryException {
        // given
        double lowerTempLimit = 12.1;
        double upperTempLimit = 15.1;
        List<Weather> weatherList = List.of(
                new Weather(3L, "03.08.2001, 11:00", upperTempLimit));
        List<String> resultList = List.of("03.08.2001, 11:00");

        given(weatherRepository.findAll()).willReturn(weatherList);

        // when
        List<String> testedResultList = underTest.findLongestTempRange(lowerTempLimit, upperTempLimit);

        // then
        assertThat(testedResultList).isEqualTo(resultList);
    }

    @Test
    void whenDbHasOneEntryOutOfRange_whenGettingLongestTemperatureRange_itShouldReturnEmptyList () throws EmptyRepositoryException {
        // given
        double lowerTempLimit = 12.1;
        double upperTempLimit = 15.1;
        List<Weather> weatherList = List.of(
                new Weather(3L, "03.08.2001, 11:00", 10.5));
        List<String> resultList = List.of();

        given(weatherRepository.findAll()).willReturn(weatherList);

        // when
        List<String> testedResultList = underTest.findLongestTempRange(lowerTempLimit, upperTempLimit);

        // then
        assertThat(testedResultList).isEqualTo(resultList);
    }

    @Test
    void itShouldGiveLongestTemperatureRange () throws EmptyRepositoryException {
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

        given(weatherRepository.findAll()).willReturn(weatherList);

        // when
        List<String> testedResultList = underTest.findLongestTempRange(lowerTempLimit, upperTempLimit);

        // then
        assertThat(testedResultList).isEqualTo(resultList);
    }

    @Test
    void whenDbEmpty_whenGettingLongestTemperatureRangeAtTime_itShouldThrowEmptyRepositoryException () {
        // given
        double lowerTempLimit = 12.1;
        double upperTempLimit = 15.1;
        String lowerHourLimit = "10:00";
        String upperHourLimit = "15:00";
        List<Weather> weatherList = List.of();

        given(weatherRepository.findAll()).willReturn(weatherList);

        // when
        EmptyRepositoryException expectedException = assertThrows(EmptyRepositoryException.class, () -> underTest.findLongestTempRangeAtTime(lowerTempLimit, upperTempLimit, lowerHourLimit, upperHourLimit));

        // then
        assertTrue(expectedException.getMessage().contains("empty"));
        assertThat(expectedException.getMessage()).isEqualTo("The database is empty.");
    }

    @Test
    void whenDbHasOneEntryAndIsInRange_whenGettingLongestTemperatureRangeAtTime_itShouldReturnOneEntry () throws EmptyRepositoryException {
        // given
        double lowerTempLimit = 12.1;
        double upperTempLimit = 15.1;
        String lowerHourLimit = "10:00";
        String upperHourLimit = "15:00";
        List<Weather> weatherList = List.of(
                new Weather(3L, "03.08.2001, 11:00", 13.5));
        List<String> resultList = List.of("03.08.2001, 11:00");

        given(weatherRepository.findAll()).willReturn(weatherList);

        // when
        List<String> testedResultList = underTest.findLongestTempRangeAtTime(lowerTempLimit, upperTempLimit, lowerHourLimit, upperHourLimit);

        // then
        assertThat(testedResultList).isEqualTo(resultList);
    }

    @Test
    void whenDbHasOneEntryAndIsEqualToLowerTempLimit_whenGettingLongestTemperatureRangeAtTime_itShouldReturnOneEntry () throws EmptyRepositoryException {
        // given
        double lowerTempLimit = 12.1;
        double upperTempLimit = 15.1;
        String lowerHourLimit = "10:00";
        String upperHourLimit = "15:00";
        List<Weather> weatherList = List.of(
                new Weather(3L, "03.08.2001, 11:00", lowerTempLimit));
        List<String> resultList = List.of("03.08.2001, 11:00");

        given(weatherRepository.findAll()).willReturn(weatherList);

        // when
        List<String> testedResultList = underTest.findLongestTempRangeAtTime(lowerTempLimit, upperTempLimit, lowerHourLimit, upperHourLimit);

        // then
        assertThat(testedResultList).isEqualTo(resultList);
    }

    @Test
    void whenDbHasOneEntryAndIsEqualToUpperTempLimit_whenGettingLongestTemperatureRangeAtTime_itShouldReturnOneEntry () throws EmptyRepositoryException {
        // given
        double lowerTempLimit = 12.1;
        double upperTempLimit = 15.1;
        String lowerHourLimit = "10:00";
        String upperHourLimit = "15:00";
        List<Weather> weatherList = List.of(
                new Weather(3L, "03.08.2001, 11:00", upperTempLimit));
        List<String> resultList = List.of("03.08.2001, 11:00");

        given(weatherRepository.findAll()).willReturn(weatherList);

        // when
        List<String> testedResultList = underTest.findLongestTempRangeAtTime(lowerTempLimit, upperTempLimit, lowerHourLimit, upperHourLimit);

        // then
        assertThat(testedResultList).isEqualTo(resultList);
    }

    @Test
    void whenDbHasOneEntryAndIsOutOfTempRange_whenGettingLongestTemperatureRangeAtTime_itShouldReturnEmptyList () throws EmptyRepositoryException {
        // given
        double lowerTempLimit = 12.1;
        double upperTempLimit = 15.1;
        String lowerHourLimit = "10:00";
        String upperHourLimit = "15:00";
        List<Weather> weatherList = List.of(
                new Weather(3L, "03.08.2001, 09:00", 10.5));
        List<String> resultList = List.of();

        given(weatherRepository.findAll()).willReturn(weatherList);

        // when
        List<String> testedResultList = underTest.findLongestTempRangeAtTime(lowerTempLimit, upperTempLimit, lowerHourLimit, upperHourLimit);

        // then
        assertThat(testedResultList).isEqualTo(resultList);
    }

    @Test
    void itShouldGiveLongestTemperatureRangeAtTime () throws EmptyRepositoryException {
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

        given(weatherRepository.findAll()).willReturn(weatherList);

        // when
        List<String> testedResultList = underTest.findLongestTempRangeAtTime(lowerTempLimit, upperTempLimit, lowerHourLimit, upperHourLimit);

        // then
        assertThat(testedResultList).isEqualTo(resultList);
    }

    // Would be possible to add more tests for different combinations of time and temperatures based on whether they
    // fall into given ranges or not
}
