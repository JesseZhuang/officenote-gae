package com.madronabearfacts.servlet;

import com.google.api.services.calendar.CalendarScopes;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.utils.SystemProperty;
import com.google.common.collect.ImmutableMap;
import com.madronabearfacts.dao.BlurbDAO;
import com.madronabearfacts.dao.SchoolYearDatesDAO;
import com.madronabearfacts.entity.Blurb;
import com.madronabearfacts.entity.Eflier;
import com.madronabearfacts.helper.Constants;
import com.madronabearfacts.helper.EflierCrawler;
import com.madronabearfacts.helper.GCalHelper;
import com.madronabearfacts.helper.GmailHelper;
import com.madronabearfacts.helper.GoogleAuthHelper;
import com.madronabearfacts.helper.MailchimpHelper;
import com.madronabearfacts.util.TimeUtils;

import javax.mail.MessagingException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ServletHelper {

    private static final Logger LOGGER = Logger.getLogger(ServletHelper.class.getName());

    public static final String TOTAL_UPDATED_COUNT = "TotalUpdatedCount", TO_BE_ARCHIVED_COUNT = "ToBeArchivedCount";

    public static int fetchBlurbs() {
        LOGGER.info("Start fetching blurbs submitted via email ...");
        List<Blurb> blurbs = GmailHelper.getBlurbs();
        BlurbDAO dao = new BlurbDAO();
        LOGGER.info(String.format("Fetched %s blurbs submitted via email.", blurbs.size()));
        LOGGER.info("Start crawling e-fliers ...");
        blurbs.addAll(buildEflierBourbWithCrawler());
        LOGGER.info(String.format("Finished crawling e-fliers total blurbs count is %s.", blurbs.size()));
        dao.saveBlurbs(blurbs, Constants.BLURB_ENTITY_KIND);
        return blurbs.size();
    }

    private static List<Blurb> buildEflierBourbWithCrawler() {
        List<Blurb> eflierBlurb = new ArrayList<>();
        final String title = "Latest Community-eFliers";
        EflierCrawler crawler = new EflierCrawler();
        List<Eflier> efliers = crawler.crawlAllEfliers();
        String content = "The following efliers are obtained from " +
                "http://www.edmonds.wednet.edu/community/community_e_fliers and error can be caused by irregular " +
                "format of the listed efliers. In case of error, you should help to contact Oscar " +
                "at halperto@edmonds.wednet.edu or (425)431-7045 and Edmonds School District " +
                "to provide feedback that the format should be kept with a standard.";
        if (!efliers.isEmpty()) {
            for (Eflier flier : efliers) content += flier;
            eflierBlurb.add(Blurb.builder().content(content).title(title).curWeek(1).numWeeks(1)
                    .fetchDate(TimeUtils.convertLocalDateToDate(LocalDate.now()))
                    .startDate(TimeUtils.convertLocalDateToDate(TimeUtils.getComingMonday()))
                    .build());
        }
        // if no e-fliers were crawled, return an empty list here.
        return eflierBlurb;
    }

    public static Map<String, Integer> updateArchiveDeleteBlurbs() {
        LOGGER.info("Start updating and archiving blurbs ...");
        BlurbDAO dao = new BlurbDAO();
        List<Blurb> blurbs = dao.getBlurbs(Constants.BLURB_ENTITY_KIND);
        List<Blurb> toBeArchivedBlurbs = new ArrayList<>();
        List<Key> toDelete = new ArrayList<>();
        LOGGER.info(String.format("Total blurbs count %s.", blurbs.size()));
        for (Blurb b : blurbs) {
            b.update();
            if (b.getCurWeek() > b.getNumWeeks()) {
                toDelete.add(KeyFactory.createKey(Constants.BLURB_ENTITY_KIND, b.getId()));
                toBeArchivedBlurbs.add(b);
            }
        }
        LOGGER.info(String.format("To be archived blurbs count %s.", toDelete.size()));
        LOGGER.info(String.format("StayOn blurbs count %s.", blurbs.size() - toDelete.size()));
        dao.updateArchiveDelete(blurbs, toBeArchivedBlurbs, toDelete);
        LOGGER.info("Finished updating and archiving blurbs.");
        return ImmutableMap.of(TOTAL_UPDATED_COUNT, blurbs.size(),
                TO_BE_ARCHIVED_COUNT, toDelete.size());
    }

    public static void writeSchoolYearDates() {
        if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production) {
            // Production
        } else {
            SchoolYearDatesDAO dao = new SchoolYearDatesDAO();
            dao.writeStartDate();
        }
    }

    public static String mailchimp() throws IOException {
        BlurbDAO dao = new BlurbDAO();
        List<Blurb> blurbs = dao.getBlurbs(Constants.BLURB_ENTITY_KIND);
        String campaignUrl = new MailchimpHelper().doAllCampaignJobs(
                GCalHelper.getCalendarService(GoogleAuthHelper.getCredServiceAccountFromClassPath(
                        CalendarScopes.CALENDAR_READONLY)),
                blurbs);
        return campaignUrl;
    }

    public static boolean sendEmailConfirmation(String campaignUrl) throws IOException, MessagingException {
        return GmailHelper.sendMessage(GoogleAuthHelper.getGmailService(), "This week's office notes prepared",
                String.format("The office notes can be previewed at <a href=\"%s\" target=\"_blank\">%s</a>",
                        campaignUrl, campaignUrl));
    }

    public static boolean sendEmailConfirmationLocal(String campaignUrl) throws IOException, MessagingException {
        return GmailHelper.sendMessageLocal(GoogleAuthHelper.getGmailService(), "This week's office notes prepared",
                String.format("The office notes can be previewed at <a href=\"%s\" target=\"_blank\">%s</a>",
                        campaignUrl, campaignUrl));
    }

    public static void main(String[] args) {
        System.out.println((String) null);
        System.out.println(((String) null) == null);
        Blurb b = Blurb.builder().content("c").title("t").curWeek(1).numWeeks(4).fetchDate(new Date())
                .startDate(new Date()).flierLinks((String) null).build();
        System.out.println(b);
        System.out.println(b.getFlierLinks() == null);
    }
}
