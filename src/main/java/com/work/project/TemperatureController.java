package com.work.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/temperature")
public class TemperatureController {

    @Autowired
    TemperatureService temperatureService;

    @PostMapping
    public void addTemperature(@RequestBody Temperature temperature) {
        temperatureService.addTemperature(temperature);
    }
}
