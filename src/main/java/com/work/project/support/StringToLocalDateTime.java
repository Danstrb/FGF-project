package com.work.project.support;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StringToLocalDateTime {
    public static LocalDateTime doFormat (String dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm");
        return LocalDateTime.parse(dateTime, formatter);
    }
}
