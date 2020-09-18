package com.example.test.utils;

public class DateUtils {
    public static String convertIntToTimeFormat(int time) {
        int h = time / 3600;
        int m = time / 60;
        int s = time % 60;
        String newTime = h + ":" + m + ":" + s;
        return newTime;
    }
}
