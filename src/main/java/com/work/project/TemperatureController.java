package com.work.project;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/temperatures")
@AllArgsConstructor
public class TemperatureController {
    TemperatureService temperatureService;

    @GetMapping
    public List<Temperature> getTemperatures () {
        return temperatureService.getTemperatures();
    }

    @PostMapping
    public void addTemperature(@RequestBody Temperature temperature) {
        temperatureService.addTemperature(temperature);
    }

    @DeleteMapping("/{id}")
    public void deleteTemperature(@PathVariable("id") Long id) {
        temperatureService.deleteTemperature(id);
    }
}
