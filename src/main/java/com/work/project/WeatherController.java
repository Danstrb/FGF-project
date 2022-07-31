package com.work.project;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/weather")
@AllArgsConstructor
public class WeatherController {
    private WeatherService temperatureService;

    @GetMapping
    public List<Weather> getAllWeather() {
        return temperatureService.getAllWeather();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Weather addWeather(@RequestBody Weather weather) {
        return temperatureService.addWeather(weather);
    }

    @PutMapping("/{id}")
    public void updateWeather(@PathVariable("id") Long id, @RequestBody Weather weather) {
        temperatureService.updateWeather(id, weather);
    }

    @DeleteMapping("/{id}")
    public void deleteTemperature(@PathVariable("id") Long id) {
        temperatureService.deleteWeather(id);
    }

    @GetMapping("/temprange")
    @ResponseBody
    public List<String> getTemperatureRange(@RequestParam float lowerTempLimit, @RequestParam float upperTempLimit) {
        return temperatureService.findLongestTempRange(lowerTempLimit, upperTempLimit);
    }

    @GetMapping("/temprange/timelimit")
    @ResponseBody
    public List<String> getTemperatureRangeAtTime(@RequestParam float lowerTempLimit,
                                                  @RequestParam float upperTempLimit,
                                                  @RequestParam String lowerTimeLimit,
                                                  @RequestParam String upperTimeLimit) {
        return temperatureService.findLongestTempRangeAtTime(lowerTempLimit, upperTempLimit, lowerTimeLimit, upperTimeLimit);
    }

}
