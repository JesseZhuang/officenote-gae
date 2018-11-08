package com.madronabearfacts.dao;

import com.google.appengine.api.datastore.Entity;
import com.madronabearfacts.util.TimeUtils;

import java.time.LocalDate;
import java.util.Date;

public class SchoolYearDatesDAO implements CloudStoreDAO {
    private static final String KIND = "SchoolYearDates";
    private static final long ID = 5629499534213120L;
    private static final String START_DATE = "SchoolYearStartDate";
    private static final String END_DATE = "SchoolYearEndDate";
    private static final String SPRING_BREAK = "SpringBreakMondayDate";

    public SchoolYearDatesDAO() {
    }

    public void writeStartDate() {
        writeDate(KIND, ID, START_DATE, TimeUtils.convertLocalDateToDate(LocalDate.of(2018, 1, 1)));
    }

    public Date getStartDate() {
        return getDate(START_DATE);
    }

    public Date getEndDate() {
        return getDate(END_DATE);
    }

    public Date getSpringBreakMonday() {
        return getDate(SPRING_BREAK);
    }

    private Date getDate(String whichDate) {
        return getDate(KIND, ID, whichDate);
    }

}
