package com.work.project;

import com.work.project.exceptions.EmptyRepositoryException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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

    public List<String> findLongestTempRange(double lowerTemperatureLimit, double higherTemperatureLimit) {
        List<String> result = new ArrayList<>();
        List<Weather> weatherList = weatherRepository.findAll();

        if (weatherList.size() == 0)
            try {
                throw new EmptyRepositoryException("The database is empty.");
            } catch (EmptyRepositoryException e) {
                e.printStackTrace();
            }
        if (weatherList.size() == 1 &&
                (weatherList.get(0).getTemperature() > lowerTemperatureLimit
                        && weatherList.get(0).getTemperature() < higherTemperatureLimit)) {
            result.add(String.valueOf(weatherList.get(0).getDateTime()));
            return result;
        }

        List<Weather> chronologicalWeatherList = weatherList.stream()
                .sorted(Comparator.comparing(w -> dateStringReverser(w.getDateTime())))
                .collect(Collectors.toList());

        List<Integer> outOfRangeIndexes = new ArrayList<>();
        outOfRangeIndexes.add(-1);
        //Populate the list - outOfRangeIndexes - that will contain all the indexes of chronologicalWeatherList, that do not comply with the given temperatures
        for (int i = 0; i < chronologicalWeatherList.size(); i++) {
            var weather = chronologicalWeatherList.get(i);
            if (weather.getTemperature() < lowerTemperatureLimit || weather.getTemperature() > higherTemperatureLimit)
                outOfRangeIndexes.add(i);
        }
        outOfRangeIndexes.add(chronologicalWeatherList.size());

        //Create a map, key is the longest period that complies with given temps, value are between which indexes
        int longestRange = 0;
        HashMap<Integer, List<Integer>> ranges = new HashMap<>();
        for (int i = outOfRangeIndexes.size()-1; i > 0; i--) {
            int range = outOfRangeIndexes.get(i) - outOfRangeIndexes.get(i-1);
            List<Integer> values = new ArrayList<>();
            longestRange = Math.max(longestRange, range);

            if (ranges.containsKey(range)) // To deal with the case when the same range already exists
                values.addAll(ranges.get(range));
            values.add(outOfRangeIndexes.get(i-1)+1); // -1 and +1 serves to move the limits, else i include the outOfBoundsLimits
            values.add(outOfRangeIndexes.get(i)-1);
            ranges.put(range, values);
        }

        var rangeIndices = ranges.get(longestRange);
        for (int i = 0; i < ranges.get(longestRange).size()-1; i+=2) {

            result.add(chronologicalWeatherList.get(rangeIndices.get(i)).getDateTime() + " to " +
                    chronologicalWeatherList.get(rangeIndices.get(i+1)).getDateTime());
        }

        return result;
    }

    public List<String> findLongestTempRangeAtTime(double lowerTemperatureLimit, double higherTemperatureLimit, String lowerHourLimit, String upperHourLimit) {
        LocalTime lowerTimeLimitFormatted = LocalTime.parse(lowerHourLimit);
        LocalTime upperTimeLimitFormatted = LocalTime.parse(upperHourLimit);

        List<Weather> weatherList = weatherRepository.findAll();
        List<Weather> chronologicalWeatherList = weatherList.stream()
                .sorted(Comparator.comparing(w -> dateStringReverser(w.getDateTime())))
                .collect(Collectors.toList());
        List<String> result = new ArrayList<>();

        if (chronologicalWeatherList.size() == 0)
            try {
                throw new EmptyRepositoryException("The database is empty.");
            } catch (EmptyRepositoryException e) {
                e.printStackTrace();
            }
        if (chronologicalWeatherList.size() == 1 &&
                ((chronologicalWeatherList.get(0).getTemperature() > lowerTemperatureLimit &&
                        chronologicalWeatherList.get(0).getTemperature() < higherTemperatureLimit) &&
                 (formatToLocalDateTime(chronologicalWeatherList.get(0).getDateTime()).toLocalTime().isAfter(lowerTimeLimitFormatted) &&
                         formatToLocalDateTime(chronologicalWeatherList.get(0).getDateTime()).toLocalTime().isBefore(upperTimeLimitFormatted)))) {
            result.add(String.valueOf(chronologicalWeatherList.get(0).getDateTime()));
            return result;
        }

        List<Integer> outOfRangeIndexes = new ArrayList<>();
        outOfRangeIndexes.add(-1);
        //Populate the list - outOfRangeIndexes - that will contain all the indexes of chronologicalWeatherList, that do not comply with the given temperatures
        for (int i = 0; i < chronologicalWeatherList.size(); i++) {
            var weather = chronologicalWeatherList.get(i);
            if ((weather.getTemperature() < lowerTemperatureLimit || weather.getTemperature() > higherTemperatureLimit) ||
                    (formatToLocalDateTime(weather.getDateTime()).toLocalTime().isBefore(lowerTimeLimitFormatted) || formatToLocalDateTime(weather.getDateTime()).toLocalTime().isAfter(upperTimeLimitFormatted)))
                outOfRangeIndexes.add(i);
        }
        outOfRangeIndexes.add(chronologicalWeatherList.size());

        //Create a map, key is the longest period that complies with given temps, value are between which indexes
        int longestRange = 0;
        HashMap<Integer, List<Integer>> ranges = new HashMap<>();
        for (int i = outOfRangeIndexes.size()-1; i > 0; i--) {
            int range = outOfRangeIndexes.get(i) - outOfRangeIndexes.get(i-1);
            List<Integer> values = new ArrayList<>();
            longestRange = Math.max(longestRange, range);

            if (ranges.containsKey(range))
                values.addAll(ranges.get(range));
            values.add(outOfRangeIndexes.get(i-1)+1); // -1 and +1 serves to move the limits, else i include the outOfBoundsLimits
            values.add(outOfRangeIndexes.get(i)-1);
            ranges.put(range, values);
        }

        var rangeIndices = ranges.get(longestRange);
        for (int i = 0; i < ranges.get(longestRange).size()-1; i+=2) {

            result.add(chronologicalWeatherList.get(rangeIndices.get(i)).getDateTime() + " to " +
                    chronologicalWeatherList.get(rangeIndices.get(i+1)).getDateTime());
        }

        return result;
    }

    private LocalDateTime formatToLocalDateTime(String dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm:ss");
        return LocalDateTime.parse(dateTime, formatter);
    }

    private static String dateStringReverser(String string) {
        StringBuilder builder = new StringBuilder();
        return builder.append(string, 6,10).append('.').append(string, 3, 5).append('.').append(string, 0, 2).toString();
    }
}
