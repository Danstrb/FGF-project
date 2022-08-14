package com.work.project;

import com.work.project.exceptions.EmptyRepositoryException;
import com.work.project.support.FindLongestRange;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class WeatherService {
    private WeatherRepository weatherRepository;

    public Weather addWeather(Weather weather) {
        return weatherRepository.save(weather);
    }

    public List<Weather> getAllWeather() {
        return weatherRepository.findAll();
    }

    public Optional<Weather> getWeatherById(Long id) {
        return weatherRepository.findById(id);
    }

    public Weather updateWeather(Weather updatedWeather) {
        return weatherRepository.save(updatedWeather);
    }

    public void deleteWeather(Long id) {
        weatherRepository.deleteById(id);
    }
    // The algorithm assumes the database is complete for a given time period (every day has 1x measurement of temperature)

    public List<String> findLongestTempRange(double lowerTemperatureLimit, double higherTemperatureLimit) throws EmptyRepositoryException {
        return FindLongestRange.findLongestTempRange(weatherRepository.findAll(), lowerTemperatureLimit, higherTemperatureLimit);
    }

    public List<String> findLongestTempRangeAtTime(double lowerTemperatureLimit, double higherTemperatureLimit, String lowerHourLimit, String upperHourLimit) throws EmptyRepositoryException {
        return FindLongestRange.findLongestTempRangeAtTime(weatherRepository.findAll(), lowerTemperatureLimit, higherTemperatureLimit, lowerHourLimit, upperHourLimit);
    }
}
