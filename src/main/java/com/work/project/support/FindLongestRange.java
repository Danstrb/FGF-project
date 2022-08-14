package com.work.project.support;

import com.work.project.Weather;
import com.work.project.exceptions.EmptyRepositoryException;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class FindLongestRange {
    public static List<String> findLongestTempRange(List<Weather> weatherList, double lowerTemperatureLimit, double higherTemperatureLimit) throws EmptyRepositoryException {
        List<String> result = new ArrayList<>();

        if (weatherList.size() == 0)
            throw new EmptyRepositoryException("The database is empty.");

        if (weatherList.size() == 1)
            if (weatherList.get(0).getTemperature() >= lowerTemperatureLimit
                    && weatherList.get(0).getTemperature() <= higherTemperatureLimit) {
                result.add(String.valueOf(weatherList.get(0).getDateTime()));
                return result;
            }

            else if (weatherList.size() == 1 &&
                    (weatherList.get(0).getTemperature() < lowerTemperatureLimit
                            || weatherList.get(0).getTemperature() > higherTemperatureLimit)) {
                return result;
            }

        List<Weather> chronologicalWeatherList = weatherList.stream()
                .sorted(Comparator.comparing(w -> DateStringReverser.reverse(w.getDateTime())))
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
        //In the below case - there is no measurement, that complies with given temperature/time combination (+2 for 'edges on 'indexes' -1 and last+1)
        if (outOfRangeIndexes.size() == chronologicalWeatherList.size()+2)
            return result;

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
        for (int i = 0; i < ranges.get(longestRange).size()-1; i+=2)
            result.add(chronologicalWeatherList.get(rangeIndices.get(i)).getDateTime() + " to " +
                    chronologicalWeatherList.get(rangeIndices.get(i+1)).getDateTime());

        return result;
    }

    public static List<String> findLongestTempRangeAtTime(List<Weather> weatherList, double lowerTemperatureLimit, double higherTemperatureLimit, String lowerHourLimit, String upperHourLimit) throws EmptyRepositoryException {
        LocalTime lowerTimeLimitFormatted = LocalTime.parse(lowerHourLimit);
        LocalTime upperTimeLimitFormatted = LocalTime.parse(upperHourLimit);

        List<Weather> chronologicalWeatherList = weatherList.stream()
                .sorted(Comparator.comparing(w -> DateStringReverser.reverse(w.getDateTime())))
                .collect(Collectors.toList());
        List<String> result = new ArrayList<>();

        if (chronologicalWeatherList.size() == 0)
            throw new EmptyRepositoryException("The database is empty.");
        if (chronologicalWeatherList.size() == 1) {
            if ((chronologicalWeatherList.get(0).getTemperature() >= lowerTemperatureLimit &&
                    chronologicalWeatherList.get(0).getTemperature() <= higherTemperatureLimit) &&
                    (StringToLocalDateTime.doFormat(chronologicalWeatherList.get(0).getDateTime()).toLocalTime().isAfter(lowerTimeLimitFormatted) &&
                            StringToLocalDateTime.doFormat(chronologicalWeatherList.get(0).getDateTime()).toLocalTime().isBefore(upperTimeLimitFormatted)))
                result.add(String.valueOf(chronologicalWeatherList.get(0).getDateTime()));
            return result;
        }

        List<Integer> outOfRangeIndexes = new ArrayList<>();
        outOfRangeIndexes.add(-1);
        //Populate the list - outOfRangeIndexes - that will contain all the indexes of chronologicalWeatherList, that do not comply with the given temperatures
        for (int i = 0; i < chronologicalWeatherList.size(); i++) {
            var weather = chronologicalWeatherList.get(i);
            if ((weather.getTemperature() < lowerTemperatureLimit || weather.getTemperature() > higherTemperatureLimit) ||
                    (StringToLocalDateTime.doFormat(weather.getDateTime()).toLocalTime().isBefore(lowerTimeLimitFormatted) || StringToLocalDateTime.doFormat(weather.getDateTime()).toLocalTime().isAfter(upperTimeLimitFormatted)))
                outOfRangeIndexes.add(i);
        }
        outOfRangeIndexes.add(chronologicalWeatherList.size());
        //In the below case - there is no measurement, that complies with given temperature/time combination (+2 for 'edges on 'indexes' -1 and last+1)
        if (outOfRangeIndexes.size() == chronologicalWeatherList.size()+2)
            return result;

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
}
