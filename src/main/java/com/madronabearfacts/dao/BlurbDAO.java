package com.madronabearfacts.dao;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.madronabearfacts.entity.Blurb;
import com.madronabearfacts.helper.Constants;
import com.madronabearfacts.util.TimeUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.madronabearfacts.entity.Blurb.*;

public class BlurbDAO implements CloudStoreDAO {
    /**
     * Saves a list of Blurbs to google cloud datastore. Keys to be auto generated.
     */
    public void saveBlurbs(List<Blurb> blurbs, String kind) {
        List<Entity> entities = blurbsToEntities(blurbs, kind);
        TransactionOptions options = TransactionOptions.Builder.withXG(true);
        Transaction txn = datastoreService.beginTransaction(options);
        try {
            datastoreService.put(txn, entities);
            txn.commit();
        } finally {
            if (txn.isActive()) txn.rollback();
        }
    }

    /**
     * Update a list of blurbs. The blurbs must have keys populated. Only active blurbs of kind "Blurb"
     * will be updated.
     *
     * @param blurbs the blurbs to be updated.
     */
    public void updateBlurbs(List<Blurb> blurbs) {
        if (blurbs.stream().anyMatch(b -> b.getId() == 0))
            throw new RuntimeException("One or more of the blurbs has a id of 0.");
        TransactionOptions options = TransactionOptions.Builder.withXG(true);
        Transaction txn = datastoreService.beginTransaction(options);
        try {
            datastoreService.put(txn, blurbsToEntitiesWithIds(blurbs, Constants.BLURB_ENTITY_KIND));
            txn.commit();
        } finally {
            if (txn.isActive()) txn.rollback();
        }
    }

    public void updateArchiveDelete(List<Blurb> toUpdate, List<Blurb> toArchive, List<Key> toDelete) {
        if (toUpdate.stream().anyMatch(b -> b.getId() == 0) || toArchive.stream().anyMatch(b -> b.getId() == 0))
            throw new RuntimeException("One or more of the blurbs has a id of 0.");
        TransactionOptions options = TransactionOptions.Builder.withXG(true);
        Transaction txn = datastoreService.beginTransaction(options);
        try {
            // update all blurbs
            datastoreService.put(txn, blurbsToEntitiesWithIds(toUpdate, Constants.BLURB_ENTITY_KIND));
            // save blurbs to be archived in a different table/kind in cloud datastore
            SchoolYearDatesDAO sydDao = new SchoolYearDatesDAO();
            int schoolYear = TimeUtils.convertDateToLocalDate(sydDao.getStartDate()).getYear();
            final String archivedBlurbKind = Constants.BLURB_ENTITY_KIND + schoolYear;
            datastoreService.put(txn, blurbsToEntitiesWithIds(toArchive, archivedBlurbKind));
            // delete archived blurbs from the active blurbs table/kind
            datastoreService.delete(txn, toDelete);
            txn.commit();
        } finally {
            if (txn.isActive()) txn.rollback();
        }
    }

    public List<Blurb> getBlurbs(String kind) {
        Query q = new Query(kind);
        PreparedQuery pq = datastoreService.prepare(q);
        List<Entity> entities = pq.asList(FetchOptions.Builder.withDefaults());
        return entitiesToBlurbs(entities);
    }

    private static List<Blurb> entitiesToBlurbs(List<Entity> entities) {
        List<Blurb> blurbs = new ArrayList<>();
        for (Entity e : entities) {
            Blurb b = Blurb.builder()
                    .title((String) e.getProperty(TITLE)).content(((Text) e.getProperty(CONTENT)).getValue())
                    .numWeeks(((Long) e.getProperty(NUM_WEEKS)).intValue())
                    .curWeek(((Long) e.getProperty(CUR_WEEK)).intValue())
                    .flierLinks((String) e.getProperty(FLIER_LINKS)).imageUrl((String) e.getProperty(IMAGE_URL))
                    .startDate((Date) e.getProperty(START_DATE)).fetchDate((Date) e.getProperty(FETCH_DATE))
                    .id(e.getKey().getId())
                    .build();
            blurbs.add(b);
        }
        return blurbs;
    }

    private static List<Entity> blurbsToEntities(List<Blurb> blurbs, String kind) {
        List<Entity> entities = new ArrayList<>();
        // use auto generate keys and key ids so do not set them here
        for (Blurb blurb : blurbs) entities.add(setEntityProperties(new Entity(kind), blurb));
        return entities;
    }

    /**
     * Convert blurbs with Ids already populated to entities.
     *
     * @param blurbs to be converted.
     * @return entities constructed from the blurbs.
     */
    private static List<Entity> blurbsToEntitiesWithIds(List<Blurb> blurbs, String kind) {
        List<Entity> entities = new ArrayList<>();
        for (Blurb blurb : blurbs) {
            Entity e = setEntityProperties(new Entity(kind, blurb.getId()), blurb);
            entities.add(e);
        }
        return entities;
    }

    private static Entity setEntityProperties(Entity entity, Blurb blurb) {
        entity.setProperty(TITLE, blurb.getTitle());
        entity.setProperty(CONTENT, new Text(blurb.getContent()));
        entity.setProperty(NUM_WEEKS, blurb.getNumWeeks());
        entity.setProperty(CUR_WEEK, blurb.getCurWeek());
        entity.setProperty(FLIER_LINKS, blurb.getFlierLinks());
        entity.setProperty(IMAGE_URL, blurb.getImageUrl());
        entity.setProperty(START_DATE, blurb.getStartDate());
        entity.setProperty(FETCH_DATE, blurb.getFetchDate());
        return entity;
    }
}