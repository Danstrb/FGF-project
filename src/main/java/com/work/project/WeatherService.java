package com.work.project;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class WeatherService {
    WeatherRepository temperatureRepository;

    public List<Weather> getAllWeather() {
        return temperatureRepository.findAll();
    }

    public void addWeather(Weather weather) {
        temperatureRepository.save(weather);
    }

    public void updateWeather(Long id, Weather weather) {
        if (!temperatureRepository.existsById(id))
            throw new IllegalArgumentException("Item does not exist");
        Weather oldWeather = temperatureRepository.findById(id).get();

        oldWeather.setTemperature(weather.getTemperature());
        oldWeather.setTime(weather.getTime());
        temperatureRepository.save(oldWeather);
    }

    public void deleteWeather(Long id) {
        temperatureRepository.deleteById(id);
    }

}
