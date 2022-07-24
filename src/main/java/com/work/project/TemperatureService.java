package com.work.project;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class TemperatureService {
    TemperatureRepository temperatureRepository;

    public List<Temperature> getTemperatures() {
        return temperatureRepository.findAll();
    }

    public void addTemperature(Temperature temperature) {
        temperatureRepository.save(temperature);
    }

}
