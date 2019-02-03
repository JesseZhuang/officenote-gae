package com.madronabearfacts.entity;

import lombok.Builder;
import lombok.NonNull;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.util.Date;

@Value
@Builder
public class Blurb {

    public static final String TITLE = "title";
    public static final String CONTENT = "content";
    public static final String NUM_WEEKS = "numWeeks";
    public static final String CUR_WEEK = "curWeek";
    public static final String FLIER_LINKS = "flierLinks";
    public static final String IMAGE_URL = "imageUrl";
    public static final String START_DATE = "startDate";
    public static final String FETCH_DATE = "fetchDate";
    public static final String SUBMITTER = "submitterEmail";
    public static final String SINGLE_BLAST = "singleBlast";

    @NonNull String title;
    @NonNull String content;
    /** Number of weeks to publish in office notes. */
    @NonNull Integer numWeeks;
    /** Starting from 1, increment each week the blurb is included. */
    @NonFinal @NonNull Integer curWeek;
    /** All uploaded flyer links in html, can contain multiple links. */
    String flierLinks;
    /** The first flier that is an image, if there is one. */
    String imageUrl;
    @NonNull Date startDate;
    @NonNull Date fetchDate;
    @Setter @NonFinal long id;
    @NonNull String submitterEmail;
    @NonNull @NonFinal SingleBlast singleBlast;

    public void update() {
        this.curWeek++;
    }

    public void markSingleBlastScheduled() {
        if (this.singleBlast.equals(SingleBlast.NOT_A_BLAST))
            throw new RuntimeException("Trying to update a blurb that is not a single blast.");
        else this.singleBlast = SingleBlast.SCHEDULED;
    }

    public static void main(String[] args) {
        String test = null;
        System.out.println(test.isEmpty());
    }
}
