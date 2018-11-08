package com.madronabearfacts.sevlet;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.madronabearfacts.util.TimeUtils;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class TestUtils {
    public static final int YEAR = 2018;
    private static final Entity ENTITY = new Entity("SchoolYearDates", 5629499534213120L);
    private static final DatastoreService SERVICE = DatastoreServiceFactory.getDatastoreService();
    public static final Date START_DATE = TimeUtils.convertLocalDateToDate(LocalDate.of(YEAR, 1, 1));

    public void setUp() {
        ENTITY.setProperty("SchoolYearStartDate", START_DATE);
        SERVICE.put(ENTITY);
    }

    public List<Key> setUpEntities(List<Entity> entities) {
        setUp();
        return SERVICE.put(entities);
    }

    public void cleanUp() {
        SERVICE.delete(ENTITY.getKey());
    }

    public void cleanUpKeys(List<Key> keys) {
        cleanUp();
        SERVICE.delete(keys);
    }
}
