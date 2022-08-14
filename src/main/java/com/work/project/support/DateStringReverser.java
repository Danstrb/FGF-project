package com.work.project.support;

public class DateStringReverser {
    public static String reverse (String string) {
        StringBuilder builder = new StringBuilder();
        return builder.append(string, 6,10).append('.').append(string, 3, 5).append('.').append(string, 0, 2).toString();
    }
}
