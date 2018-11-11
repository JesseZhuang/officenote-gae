package com.madronabearfacts.util;

import com.madronabearfacts.helper.Constants;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Time utility functions.
 */
public class TimeUtils {

    private static final Logger LOGGER = Logger.getLogger(TimeUtils.class.getName());

    public static final DateTimeFormatter CAMPAIGN_SCHEDULE = DateTimeFormatter.ofPattern("yyyy-LL-dd'T'kk-mmxxx");
    public static final DateTimeFormatter CAMPAIGN_TITLE = DateTimeFormatter.ofPattern("yyyy-LL-dd");


    /**
     * @return the Monday before the most recent one.
     */
    public static LocalDate getLastMonday() {
        return getComingMonday(LocalDate.now()).minusDays(14);
    }

    public static LocalDate getComingMonday(LocalDate today) {
        int old = today.getDayOfWeek().getValue();
        return today.plusDays(8 - old);
    }

    public static LocalDate getTheNextBusinessDay(LocalDate today) {
        LocalDate nextBysinessDay = today.plusDays(2);
        if (nextBysinessDay.getDayOfWeek() == DayOfWeek.SATURDAY) return nextBysinessDay.plusDays(2);
        if (nextBysinessDay.getDayOfWeek() == DayOfWeek.SUNDAY) return nextBysinessDay.plusDays(1);
        else return nextBysinessDay;
    }

    public static long getTwoMonthsFromNow(LocalDate today) {
        return today.plusMonths(2).atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000;
    }

    public static ZonedDateTime getComingMonday6am(LocalDate today) {
        // office notes goes out every Monday 6 am pacific time
        int hour = Constants.isLocalDev ? 6 : 14;
        LocalDateTime monday6am = getComingMonday(today).atTime(hour, 0);
        LOGGER.info("ZoneId " + ZoneId.systemDefault()); // GAE personal account uses ZoneId.UTC
        return ZonedDateTime.of(monday6am, ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC);
//        return monday6am.atZone(ZoneId.of("Z"));
    }

    public static ZonedDateTime getTheNextBusinessDay6am(LocalDate today) {
        int hour = Constants.isLocalDev ? 6 : 14;
        LocalDateTime next6am = getTheNextBusinessDay(today).atTime(hour, 0);
        return ZonedDateTime.of(next6am, ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC);
    }

    public static LocalDate parseDate(String date) {
        if (date == null) return null;
//        DateTimeFormatter twoDigitYear = DateTimeFormatter.ofPattern("M/d/yy");
//        DateTimeFormatter fourDigitYear = DateTimeFormatter.ofPattern("M/d/yyyy");
//        DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendOptional(fourDigitYear).
//                appendOptional(twoDigitYear).toFormatter();
        DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern("M/d/")
                .optionalStart().appendPattern("uuuu").optionalEnd()
                .optionalStart().appendValueReduced(ChronoField.YEAR, 2, 2, LocalDate.now().minusYears(80))
                .optionalEnd().toFormatter();
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("[M/d/yyyy][M/d/yy]");
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy"); // does not work for 01/05/18
        LocalDate result = null;
        try {
            result = LocalDate.parse(date, formatter);
        } catch (DateTimeParseException e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    public static LocalDate convertDateToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static Date convertLocalDateToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static void main(String[] args) {
        ZonedDateTime nowUTC = ZonedDateTime.now(ZoneOffset.UTC);

        // UTC is 7 hour ahead of Pacific time
        LOGGER.info("Right now UTC time is " + DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(nowUTC));

        for (int i = 0; i < 7; i++) {
            LocalDate start = LocalDate.now().plusDays(i);
            System.out.println(start);
            System.out.println(getComingMonday6am(start).format(CAMPAIGN_SCHEDULE) + "coming monday");
            System.out.println(getTheNextBusinessDay6am(start).format(CAMPAIGN_SCHEDULE) + "next business day");
        }

        System.out.println(LocalDate.of(2003, 5, 23));

        System.out.println(LocalDate.of(2003, 5, 23).isAfter(LocalDate.of(2003, 5, 22)));

        System.out.println(getLastMonday());
        System.out.println("year " + getLastMonday().getYear());

        System.out.println(parseDate("12/20/2017"));
        System.out.println(parseDate("01/08/2018")); //Exception with M/d/yy
        System.out.println(parseDate("01/05/18")); //0018-01-05 with M/d/y, Exception with M/d/yyyy
        System.out.println(parseDate("01/05/97")); //2097-01-05 with [M/d/yyyy][M/d/yy], ideally should be 1997
        System.out.println(parseDate("01/15/02"));
        System.out.println(parseDate("01/15/22"));
        System.out.println(parseDate("01/15/37"));
        System.out.println(parseDate("01/15/38"));
    }
}
