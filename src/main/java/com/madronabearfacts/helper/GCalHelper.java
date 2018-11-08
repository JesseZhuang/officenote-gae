package com.madronabearfacts.helper;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;

import java.io.IOException;
import java.util.Collections;

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

    public static void main(String[] args) throws IOException {
        System.out.println(getCalendarService(GoogleAuthHelper
                .getCredServiceAccountFromFilePath(Collections.singleton(CalendarScopes.CALENDAR_READONLY))));
    }
}
