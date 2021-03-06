package com.madronabearfacts.dao;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generic google cloud data store DAO. For delete API, keys are required so
 */
public interface CloudStoreDAO {
    DatastoreService datastoreService = DatastoreSingleton.getInstance();
    String rootKeyName = "parent";
    Logger logger = Logger.getLogger(CloudStoreDAO.class.getName());

    default void deleteEntities(List<Key> keys) {
        TransactionOptions options = TransactionOptions.Builder.withXG(true);
        Transaction txn = datastoreService.beginTransaction(options);
        try {
            datastoreService.delete(txn, keys);
            txn.commit();
        } finally {
            if (txn.isActive()) txn.rollback();
        }
    }

    default Date getDate(String kind, long id, String propertyName) {
        try {
            Entity entity = datastoreService.get(KeyFactory.createKey(kind, id));
            return (Date) entity.getProperty(propertyName);
        } catch (EntityNotFoundException e) {
            logger.log(Level.SEVERE, "exception ", e);
            throw new RuntimeException(String.format("Cannot find %s in cloud datastore for kind : %s.",
                    propertyName, kind));
        }
    }

    default Date getDate(String kind, Key key, String propertyName) {
        try {
            Entity entity = datastoreService.get(key);
            return (Date) entity.getProperty(propertyName);
        } catch (EntityNotFoundException e) {
            logger.log(Level.SEVERE, "exception ", e);
            throw new RuntimeException(String.format("Cannot find %s in cloud datastore for kind : %s.",
                    propertyName, kind));
        }
    }

    default void writeDate(String kind, long id, String propertyName, Date date) {
        Entity entity = new Entity(kind, id);
        entity.setProperty(propertyName, date);
        datastoreService.put(entity);
    }

    default void writeDate(String kind, Key key, String propertyName, Date date) {
        Entity entity = new Entity(key);
        entity.setProperty(propertyName, date);
        datastoreService.put(entity);
    }

    default void writeDates(String kind, long id, Map<String, Date> dates) {
        Entity e = new Entity(kind, id);
        dates.forEach((property, date) -> {
            e.setProperty(property, date);
        });
        datastoreService.put(e);
    }
}
