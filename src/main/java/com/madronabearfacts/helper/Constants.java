package com.madronabearfacts.helper;

import com.google.appengine.api.utils.SystemProperty;
import com.madronabearfacts.util.FileUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class Constants {
    /**
     * <p>Files in src/main/resources are copied by maven into officenotes-1.0-SNAPSHOT.war at /WEB-INF/classes,
     * which is the classpath.
     */
    public static final String resouceFilePath = "src/main/resources/";

    // Eflier related
    public static final String ESD_DOMAIN = "https://www.edmonds.wednet.edu";
    public static final String EFLIER_PATH = "/community/community_e_fliers/";
    public static final String UNKOWN_DATE = "bad_date_format";
    public static final List<String> EFLIER_CATEGORIES = Arrays.asList(
            "community_activites_events_and_information",
            "lessons_and_classes",
            "youth_organizations",
            "sports__camps__lessons__and_teams",
            "summer_activities_-_district___community");
    public static List<String> EFLIER_URLS;

    static {
        EFLIER_URLS = new ArrayList<>();
        String prefix = ESD_DOMAIN + EFLIER_PATH;
        for (int i = 0; i < EFLIER_CATEGORIES.size(); i++) {
            EFLIER_URLS.add(prefix + EFLIER_CATEGORIES.get(i));
        }
    }

    // properties
    public static final Properties MAILCHIMP = FileUtils.loadClassPathProperty("/mailchimp.properties");
    public static final Properties GOOGLE = FileUtils.loadClassPathProperty("/google.properties");

    public static final boolean isLocalDev =
            SystemProperty.environment.value() != SystemProperty.Environment.Value.Production;

    public static void main(String[] args) {
    }
}
