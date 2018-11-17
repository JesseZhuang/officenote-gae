package com.madronabearfacts.dao;

import com.google.common.collect.ImmutableMap;
import com.madronabearfacts.util.TimeUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SchoolYearDatesDAO implements CloudStoreDAO {
    private static final String KIND = "SchoolYearDates";
    private static final long ID = 5629499534213120L;
    private static final String START_DATE = "SchoolYearStart";
    private static final String END_DATE = "SchoolYearEnd";
    private static final String SPRING_BREAK = "SpringBreakMonday";
    private static final String WINTER_BREAK1 = "WinterBreakMonday1";
    private static final String WINTER_BREAK2 = "WinterBreakMonday2";

    public SchoolYearDatesDAO() {
    }

    public void writeDates() {
        LocalDate today = LocalDate.now();
        Date start = TimeUtils.convertLocalDateToDate(today.minusDays(1));
        Date end = TimeUtils.convertLocalDateToDate(today.plusMonths(2));
        Date spring = TimeUtils.convertLocalDateToDate(LocalDate.of(2019, 4, 1));
        Date winter1 = TimeUtils.convertLocalDateToDate(LocalDate.of(2018, 12, 24));
        Date winter2 = TimeUtils.convertLocalDateToDate(LocalDate.of(2018, 12, 31));

        writeDates(KIND, ID, ImmutableMap.of(START_DATE, start, END_DATE, end, SPRING_BREAK, spring,
                WINTER_BREAK1, winter1, WINTER_BREAK2, winter2));
    }

    public Date getStartDate() {
        return getDate(START_DATE);
    }

    public Date getEndDate() {
        return getDate(END_DATE);
    }

    public Date getSpringBreak() {
        return getDate(SPRING_BREAK);
    }

    public Date getWinterBreak1() {
        return getDate(WINTER_BREAK1);
    }

    public Date getWinterBreak2() {
        return getDate(WINTER_BREAK2);
    }

    public List<Date> getSkipDates() {
        List<Date> dates = new ArrayList<>();
        dates.add(getSpringBreak());
        dates.add(getWinterBreak1());
        dates.add(getWinterBreak2());
        return dates;
    }

    private Date getDate(String whichDate) {
        return getDate(KIND, ID, whichDate);
    }

}
