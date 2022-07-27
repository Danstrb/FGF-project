package com.work.project;

import com.work.project.exceptions.EmptyRepositoryException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class WeatherService {
    private WeatherRepository temperatureRepository;

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

    // TODO its kind of working, missing checks for input data, missing solution for more than one range and other possible edge cases
    // TODO refactoring for the other task, higher sample check
    public List<String> findLongestTempRange(float lowerTemperatureLimit, float higherTemperatureLimit) {
        List<Weather> weatherList = temperatureRepository.findAll();
        List<Weather> chronologicalWeatherList = weatherList.stream()
                   .sorted(Comparator.comparing(Weather::getTime))
                   .collect(Collectors.toList());
        List<String> result = new ArrayList<>();

        if (chronologicalWeatherList.size() == 0)
            try {
                throw new EmptyRepositoryException("The database is empty.");
            } catch (EmptyRepositoryException e) {
                e.printStackTrace();
            }
        if (chronologicalWeatherList.size() == 1 &&
                (chronologicalWeatherList.get(0).getTemperature() > lowerTemperatureLimit
                        && chronologicalWeatherList.get(0).getTemperature() < higherTemperatureLimit)) {
            result.add(String.valueOf(chronologicalWeatherList.get(0).getTemperature()));
            return result;
        }

        List<Integer> outOfRange = new ArrayList<>();
        outOfRange.add(-1);

        //Populate the list - outOfRange - that will contain all the indexes of chronologicalWeatherList, that do not comply with the given temperatures
        for (int i = 0; i < chronologicalWeatherList.size(); i++) {
            var weather = chronologicalWeatherList.get(i);
            if (weather.getTemperature() < lowerTemperatureLimit || weather.getTemperature() > higherTemperatureLimit)
                outOfRange.add(i);
        }

        //Create a map, key is the longest period that complies with given temps, value are between which indexes
        int longestRange = 0;
        HashMap<Integer, List<Integer>> ranges = new HashMap<>();
        for (int i = outOfRange.size()-1; i > 0; i--) { //TODO check if there is more than one thing int the outOfRange list - and other checks elsewhere
            int range = outOfRange.get(i) - outOfRange.get(i-1);
            List<Integer> values = new ArrayList<>();
            longestRange = Math.max(longestRange, range);

            if (ranges.containsKey(range)) // To deal with the case when the same range already exists - TODO: TEST THIS SHIT
                values.addAll(ranges.get(range));
            values.add(outOfRange.get(i-1)+1);
            values.add(outOfRange.get(i)-1);
            ranges.put(range, values);
                                                                                        // -1 and +1 serves to move the limits, else i include the outOfBoundsLimits
        }

        var rangeIndices = ranges.get(longestRange);
        for (int i = 0; i < ranges.get(longestRange).size()-1; i+=2) {

            result.add(chronologicalWeatherList.get(rangeIndices.get(i)).getTime() + " to " +
                    chronologicalWeatherList.get(rangeIndices.get(i+1)).getTime());
        }

        return result;
    }

}
