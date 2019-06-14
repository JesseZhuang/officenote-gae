package com.madronabearfacts.helper;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Events;
import com.madronabearfacts.util.TimeUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Properties;

import static com.madronabearfacts.helper.GoogleAuthHelper.*;

public class GCalHelper {
    /**
     * Build and return an authorized Calendar client service.
     *
     * @return an authorized Calendar client service
     */
    public static Calendar getCalendarService(GoogleCredential credential) {
        return new Calendar.Builder(GoogleAuthHelper.HTTP_TRANSPORT, GoogleAuthHelper.JSON_FACTORY,
                credential).setApplicationName(GoogleAuthHelper.APPLICATION_NAME).build();
    }

    private static final Calendar calendar;

    static {
        Properties p = Constants.GOOGLE;
        calendar = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                new GoogleCredential.Builder().setJsonFactory(JSON_FACTORY).setTransport(HTTP_TRANSPORT)
                        .setClientSecrets(p.getProperty("client_id"), p.getProperty("client_secret")).build()
                        .setAccessToken(p.getProperty("access_token")).setRefreshToken(p.getProperty("refresh_token")))
                .setApplicationName(APPLICATION_NAME).build();
    }

    public static Calendar getInstance() {
        return calendar;
    }

    public static void main(String[] args) throws IOException {
        Calendar calendar = getCalendarService(GoogleAuthHelper
                .getCredServiceAccountFromFilePath(Collections.singleton(CalendarScopes.CALENDAR_READONLY)));
        DateTime now = new DateTime(System.currentTimeMillis());
        DateTime twoMonthsFromNow = new DateTime(TimeUtils.getTwoMonthsFromNow(LocalDate.now()));
        Events events = calendar.events().list(Constants.GOOGLE.getProperty("bearfacts.gmail"))
                .setTimeMax(twoMonthsFromNow).setTimeMin(now).setOrderBy("startTime")
                .setSingleEvents(true).execute();
        System.out.println(events);
    }
}
