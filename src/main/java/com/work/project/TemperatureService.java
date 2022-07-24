package com.work.project;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TemperatureService {
    TemperatureRepository temperatureRepository;

    public void addTemperature(Temperature temperature) {
        temperatureRepository.save(temperature);
    }
}
