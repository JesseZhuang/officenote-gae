package com.madronabearfacts.servlet;

import com.google.api.services.calendar.CalendarScopes;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.utils.SystemProperty;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class ServletHelper {

    private static final Logger LOGGER = Logger.getLogger(ServletHelper.class.getName());

    public static boolean shouldExecuteCronWeekly() {
        SchoolYearDatesDAO dao = new SchoolYearDatesDAO();
        LocalDate today = LocalDate.now();
        LocalDate schoolYearStart = TimeUtils.convertDateToLocalDate(dao.getStartDate());
        LocalDate schoolYearEnd = TimeUtils.convertDateToLocalDate(dao.getEndDate());
        if (today.isBefore(schoolYearStart.minusDays(7)) || today.isAfter(schoolYearEnd)) {
            LOGGER.info("Summer time ...");
            return false;
        }
        List<Date> skips = dao.getSkipDates();
        for (Date date : skips) {
            LOGGER.info(String.format("Date to skip: %s, today: %s.", date, today));
            if (ChronoUnit.DAYS.between(TimeUtils.convertDateToLocalDate(date), today) == 0) {
                LOGGER.info("Skipping ...");
                return false;
            }
        }
        return true;
    }

    public static List<Key> fetchBlurbs() {
        LOGGER.info("Start fetching blurbs submitted via email ...");
        List<Blurb> blurbs = GmailHelper.getBlurbs();
        BlurbDAO dao = new BlurbDAO();
        LOGGER.info(String.format("Fetched %s blurbs submitted via email.", blurbs.size()));
        LOGGER.info("Start crawling e-fliers ...");
        blurbs.addAll(buildEflierBourbWithCrawler());
        LOGGER.info(String.format("Finished crawling e-fliers total blurbs count is %s.", blurbs.size()));
        return dao.saveBlurbs(blurbs, Constants.ACTIVE_BLURB_KIND);
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

    public static List<Key> updateArchiveDeleteBlurbs() {
        LOGGER.info("Start updating and archiving blurbs ...");
        BlurbDAO dao = new BlurbDAO();
        List<Blurb> blurbs = dao.getBlurbs(Constants.ACTIVE_BLURB_KIND);
        List<Blurb> toBeArchivedBlurbs = new ArrayList<>();
        List<Key> toDelete = new ArrayList<>();
        List<Key> stayOnKeys = new ArrayList<>();
        LOGGER.info(String.format("Total blurbs count %s.", blurbs.size()));
        for (Blurb b : blurbs) {
            b.update();
            if (b.getCurWeek() > b.getNumWeeks()) {
                toDelete.add(KeyFactory.createKey(Constants.BLURB_PARENT_KEY, Constants.ACTIVE_BLURB_KIND, b.getId()));
                toBeArchivedBlurbs.add(b);
            } else stayOnKeys.add(KeyFactory.createKey(
                    Constants.BLURB_PARENT_KEY, Constants.ACTIVE_BLURB_KIND, b.getId()));
        }
        LOGGER.info(String.format("To be archived blurbs count %s.", toDelete.size()));
        LOGGER.info(String.format("StayOn blurbs count %s.", stayOnKeys.size()));
        dao.updateArchiveDelete(blurbs, toBeArchivedBlurbs, toDelete);
        LOGGER.info("Finished updating and archiving blurbs.");
        return stayOnKeys;
    }

    public static void prepLocalDatastore() {
        if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production) {
            // Production
        } else {
            SchoolYearDatesDAO dao = new SchoolYearDatesDAO();
            dao.writeDates();
            BlurbDAO dao1 = new BlurbDAO();
            dao1.writeBlurbParent();
        }
    }

    public static String mailchimp(List<Key> active) throws IOException {
        BlurbDAO dao = new BlurbDAO();
        List<Blurb> blurbs = dao.getBlurbs(active);
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
        LocalDate today = LocalDate.now();
        System.out.println(today);
        LocalDate yesterday = today.minusDays(1);
        System.out.println(ChronoUnit.DAYS.between(today, yesterday));
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        LocalDate todayNight = TimeUtils.convertDateToLocalDate(calendar.getTime());
        System.out.println(ChronoUnit.DAYS.between(today, todayNight));
    }
}
