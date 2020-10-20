package com.example.hdartalk.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NoteUtils {

    /**
     * @param time that will be convert  and formatted to string
     * @return string
     */
    public static String dateFromLong(long time) {
        DateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy 'at' hh:mm aaa", Locale.FRENCH);
        return format.format(new Date(time));
    }
}