package com.madronabearfacts.dao;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

public class DatastoreSingleton {
    private DatastoreSingleton() {}
    private static class SingletonHelper {
        private static final DatastoreService INSTANCE = DatastoreServiceFactory.getDatastoreService();
    }

    public static DatastoreService getInstance() {
        return SingletonHelper.INSTANCE;
    }
}
