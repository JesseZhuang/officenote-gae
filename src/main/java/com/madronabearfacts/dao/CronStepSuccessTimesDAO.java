package com.madronabearfacts.dao;

import com.google.common.collect.ImmutableMap;

import java.util.Date;

public class CronStepSuccessTimesDAO implements CloudStoreDAO {
    private static final String KIND = "CronStepSuccessTimes";
    private static final long ID = 5646874153320448L;
    private static final String UPDATE = "UpdateAndArchiveBlurb";
    private static final String FETCH_BLURB = "FetchBlurb";
    private static final String MAILCHIMP = "Mailchimp";
    private static final String SUCCESS_ALL = "SendConfirmationEmail";

    public CronStepSuccessTimesDAO(){}

    public Date getFetchBlurbTime () {
        return getDate(KIND, ID, FETCH_BLURB);
    }

    public Date getMailchimpTime () {
        return getDate(KIND, ID, MAILCHIMP);
    }

    public Date getUpdateBlurbTime () {
        return getDate(KIND, ID, UPDATE);
    }

    public Date getEmailConfirmationTime () {
        return getDate(KIND, ID, SUCCESS_ALL);
    }

    public void writeDates(Date update, Date fetch, Date mailchimp, Date confirm) {
        writeDates(KIND, ID, ImmutableMap.of(UPDATE, update, FETCH_BLURB, fetch, MAILCHIMP, mailchimp,
                SUCCESS_ALL, confirm));
    }

}
