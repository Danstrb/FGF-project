package com.work.project;

import com.work.project.exceptions.EmptyRepositoryException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/weathers")
@AllArgsConstructor
public class WeatherController {
    private WeatherService weatherService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Weather addWeather(@RequestBody Weather weather) {
        return weatherService.addWeather(weather);
    }

    @GetMapping
    public List<Weather> getAllWeather() {
        return weatherService.getAllWeather();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Weather> getWeatherById(@PathVariable ("id") Long id) {
        return weatherService.getWeatherById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Weather> updateWeather(@PathVariable("id") Long id, @RequestBody Weather weather) {
        return weatherService.getWeatherById(id)
                .map(savedWeather -> {
                    savedWeather.setDateTime(weather.getDateTime());
                    savedWeather.setTemperature(weather.getTemperature());

                    Weather updatedWeather = weatherService.updateWeather(savedWeather);
                    return new ResponseEntity<>(updatedWeather, HttpStatus.OK);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public void deleteWeather(@PathVariable("id") Long id) {
        weatherService.deleteWeather(id);
    }

    @GetMapping("/temprange")
    @ResponseBody
    public List<String> getWeatherRange(@RequestParam double lowerTempLimit, @RequestParam double upperTempLimit) throws EmptyRepositoryException {
        return weatherService.findLongestTempRange(lowerTempLimit, upperTempLimit);
    }

    @GetMapping("/temprange/timelimit")
    @ResponseBody
    public List<String> getWeatherRangeAtTime(@RequestParam double lowerTempLimit,
                                              @RequestParam double upperTempLimit,
                                              @RequestParam String lowerTimeLimit,
                                              @RequestParam String upperTimeLimit) throws EmptyRepositoryException {
        return weatherService.findLongestTempRangeAtTime(lowerTempLimit, upperTempLimit, lowerTimeLimit, upperTimeLimit);
    }

}
