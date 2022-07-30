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
        oldWeather.setDateTime(weather.getDateTime());
        temperatureRepository.save(oldWeather);
    }

    public void deleteWeather(Long id) {
        temperatureRepository.deleteById(id);
    }

    // TODO checks for input data, possible edge cases
    // TODO refactoring for the other task, higher sample check
    // TODO what if there are no ranges, only single days that comply?
    // The algorithm assumes the database is complete for a given time period (every day has 1x measurement of temperature)

    public List<String> findLongestTempRange(float lowerTemperatureLimit, float higherTemperatureLimit) {
        List<String> result = new ArrayList<>();
        List<Weather> weatherList = temperatureRepository.findAll();

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

        //Create a map, key is the longest period that complies with given temps, value are between which indexes
        int longestRange = 0;
        HashMap<Integer, List<Integer>> ranges = new HashMap<>();
        for (int i = outOfRangeIndexes.size()-1; i > 0; i--) { //TODO check if there is more than one thing int the outOfRangeIndexes list - and other checks elsewhere
            int range = outOfRangeIndexes.get(i) - outOfRangeIndexes.get(i-1);
            List<Integer> values = new ArrayList<>();
            longestRange = Math.max(longestRange, range);

            if (ranges.containsKey(range)) // To deal with the case when the same range already exists - TODO: TEST THIS SHIT
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

        return result; //TODO: returns Date AND Time - make it return date only
    }

    public List<String> findLongestTempRangeAtTime(float lowerTemperatureLimit, float higherTemperatureLimit, String lowerHourLimit, String upperHourLimit) {
        LocalTime lowerTimeLimitFormatted = LocalTime.parse(lowerHourLimit); //TODO: If minutes were needed as well - change parameters to Strings and parse them to LocalTime
        LocalTime upperTimeLimitFormatted = LocalTime.parse(upperHourLimit);

        List<Weather> weatherList = temperatureRepository.findAll();
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
        if (chronologicalWeatherList.size() == 1 && //TODO: Refactor if possible
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

            if (ranges.containsKey(range)) // To deal with the case when the same range already exists - TODO: TEST THIS
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
        LocalDateTime dateTimeFormatted = LocalDateTime.parse(dateTime, formatter);
        return dateTimeFormatted;
    }

    private static String dateStringReverser(String string) {
        StringBuilder builder = new StringBuilder();
        return builder.append(string.substring(6,10)).append('.').append(string.substring(3,5)).append('.').append(string.substring(0,2)).toString();
    }
}
