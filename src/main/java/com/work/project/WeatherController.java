package com.work.project;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/temperatures")
@AllArgsConstructor
public class WeatherController {
    WeatherService temperatureService;

    @GetMapping
    public List<Weather> getAllWeather() {
        return temperatureService.getAllWeather();
    }

    @PostMapping
    public void addWeather(@RequestBody Weather weather) {
        temperatureService.addWeather(weather);
    }

    @PutMapping("/{id}")
    public void updateWeather(@PathVariable("id") Long id, @RequestBody Weather weather) {
        temperatureService.updateWeather(id, weather);
    }

    @DeleteMapping("/{id}")
    public void deleteTemperature(@PathVariable("id") Long id) {
        temperatureService.deleteWeather(id);
    }

}
