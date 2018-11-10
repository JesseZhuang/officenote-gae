package com.madronabearfacts.dao;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.madronabearfacts.util.TimeUtils;

import java.time.LocalDate;
import java.util.Date;

public class CronStepSuccessTimesDAO implements CloudStoreDAO {
    private static final String KIND = "CronStepSuccessTimes";
    private static final String UPDATE = "UpdateArchiveDeleteBlurb";
    private static final String FETCH_BLURB = "FetchBlurb";
    public static final Key KEY = KeyFactory.createKey(KIND, rootKeyName);

    public CronStepSuccessTimesDAO() {
    }

    public void writeForLocal() {
        Entity e = new Entity(KEY);
        e.setProperty(FETCH_BLURB, TimeUtils.convertLocalDateToDate(LocalDate.now().minusDays(1)));
        e.setProperty(UPDATE, TimeUtils.convertLocalDateToDate(LocalDate.now().minusDays(1)));
        datastoreService.put(e);
    }

    public Date getFetchBlurbTime() {
        return getDate(KIND, KEY, FETCH_BLURB);
    }

    public Date getUpdateBlurbTime() {
        return getDate(KIND, KEY, UPDATE);
    }

    public void updateFetchBlurbTime(Date date) throws EntityNotFoundException {
        update(FETCH_BLURB, date);
    }

    public void updateUpdateBlurbTime(Date date) throws EntityNotFoundException {
        update(UPDATE, date);
    }

    public void update(String property, Date date) throws EntityNotFoundException {
        Entity e = datastoreService.get(KEY);
        e.setProperty(property, date);
        datastoreService.put(e);
    }

}
