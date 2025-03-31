package com.example.happyfridge.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class DateUtils {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yy", Locale.getDefault());
    private static final SimpleDateFormat DATE_FORMAT_FULL = new SimpleDateFormat("dd MMMM yyyy", new Locale("ru"));

    public static String formatDate(Date date) {
        if (date == null) return "";
        return DATE_FORMAT.format(date);
    }

    public static String formatDateForCalendarTitle(Date date) {
        if (date == null) return "";
        return DATE_FORMAT_FULL.format(date);
    }

    public static long getDaysRemaining(Date expiryDate) {
        if (expiryDate == null) return Long.MIN_VALUE;

        long todayMillis = getStartOfDayMillis(new Date());
        long expiryMillis = getStartOfDayMillis(expiryDate);

        long diffMillis = expiryMillis - todayMillis;
        return TimeUnit.MILLISECONDS.toDays(diffMillis);
    }

    public static long getStartOfDayMillis(Date date) {
        if (date == null) return 0;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static long getEndOfDayMillis(Date date) {
        if (date == null) return 0;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }

    public static Date getStartOfDay(Date date) {
        if (date == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date calculateExpiryDate(Date manufacturingDate, int shelfLifeValue, boolean isMonths) {
        if (manufacturingDate == null || shelfLifeValue <= 0) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(manufacturingDate);
        if (isMonths) {
            calendar.add(Calendar.MONTH, shelfLifeValue);
        } else {
            calendar.add(Calendar.DAY_OF_MONTH, shelfLifeValue);
        }
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}