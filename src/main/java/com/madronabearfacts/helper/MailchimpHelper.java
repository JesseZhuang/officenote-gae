package com.madronabearfacts.helper;

import com.ecwid.maleorang.MailchimpClient;
import com.ecwid.maleorang.MailchimpException;
import com.ecwid.maleorang.method.v3_0.campaigns.CampaignActionMethod;
import com.ecwid.maleorang.method.v3_0.campaigns.CampaignInfo;
import com.ecwid.maleorang.method.v3_0.campaigns.EditCampaignMethod;
import com.ecwid.maleorang.method.v3_0.campaigns.content.SetCampaignContentMethod;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.madronabearfacts.entity.Blurb;
import com.madronabearfacts.util.FileUtils;
import com.madronabearfacts.util.HtmlUtils;
import com.madronabearfacts.util.TimeUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MailchimpHelper {
    private final Logger logger = Logger.getLogger(MailchimpHelper.class.getName());

    private static final MailchimpClient CLIENT = new MailchimpClient(Constants.MAILCHIMP.getProperty("mailchimp_key"));
    private static final String PART1 = FileUtils.readClassPathFileToString("/mailchimp_template_part1.html");
    private static final String PART2 = FileUtils.readClassPathFileToString("/mailchimp_template_part2.html");
    private static final String PART3 = FileUtils.readClassPathFileToString("/mailchimp_template_part3.html");
    private static final String CALENDAR_STICKY = FileUtils.readClassPathFileToString("/mailchimp_calendar_sticky.html");
    private static final String SINGLE_BLAST = FileUtils.readClassPathFileToString("/mailchimp_single_blast.html");

    public MailchimpHelper() {
        Logger ecwid = Logger.getLogger("com.ecwid.maleorang");
        ecwid.setLevel(Level.WARNING);
    }

    private CampaignInfo createCampaign(String campaignTitle) {
        EditCampaignMethod.Create job = new EditCampaignMethod.Create();
        job.type = CampaignInfo.Type.REGULAR;
        job.recipients = new CampaignInfo.RecipientsInfo();
        job.recipients.list_id = Constants.MAILCHIMP.getProperty("list_id");

        job.settings = new CampaignInfo.SettingsInfo();
        job.settings.subject_line = campaignTitle;
        job.settings.from_name = Constants.MAILCHIMP.getProperty("from_name");
        job.settings.reply_to = Constants.MAILCHIMP.getProperty("reply_to");
        job.settings.folder_id = Constants.MAILCHIMP.getProperty("folder_id");
        job.settings.auto_tweet = Boolean.valueOf(Constants.MAILCHIMP.getProperty("auto_tweet"));
        job.settings.title = campaignTitle;
        job.settings.to_name = Constants.MAILCHIMP.getProperty("to_name");

        job.social_card = new CampaignInfo.SocialCardInfo();
        job.social_card.image_url = Constants.MAILCHIMP.getProperty("social_url");
        job.social_card.description = Constants.MAILCHIMP.getProperty("social_description");
        job.social_card.title = campaignTitle;

        CampaignInfo campaignInfo;
        try {
            campaignInfo = CLIENT.execute(job);
        } catch (IOException | MailchimpException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create campaign.");
        }
        logger.info("Created campaign with id " + campaignInfo.id);
        return campaignInfo;
    }

    private void setCampaignCotent(String campaignId, String html) {
        SetCampaignContentMethod job = new SetCampaignContentMethod(campaignId);
        job.html = html;
        try {
            CLIENT.execute(job);
        } catch (IOException | MailchimpException e) {
            e.printStackTrace();
            throw new RuntimeException("Set campaign content failed for " + campaignId);
        }
    }

    private void scheduleCampaign(String campaignId, ZonedDateTime time) {

        CampaignActionMethod.Schedule job = new CampaignActionMethod.Schedule(campaignId);
        job.schedule_time = Date.from(time.toInstant());
        try {
            CLIENT.execute(job);
        } catch (IOException | MailchimpException e) {
            e.printStackTrace();
            throw new RuntimeException("Schedule campaign failed for " + campaignId);
        }

    }

    private String generateRightColumn(List<Blurb> blurbs) {
        logger.info(String.format("Total blurbs count is %s.", blurbs.size()));
        blurbs.forEach(b -> logger.info("Blurb id " + b.getId()));
        // sort blurbs into 4 categories
        List<Blurb> thisWeek = new ArrayList<>(), lastWeekOnNotes = new ArrayList<>(),
                pastWeeks = new ArrayList<>(), community = new ArrayList<>();
        for (Blurb blurb : blurbs) {
            if (blurb.getTitle().contains("Community-")) community.add(blurb);
            else if (blurb.getCurWeek() == 1) thisWeek.add(blurb);
            else if (blurb.getCurWeek().equals(blurb.getNumWeeks())) lastWeekOnNotes.add(blurb);
            else pastWeeks.add(blurb);
        }
        blurbs = new ArrayList<>();
        blurbs.addAll(thisWeek);
        logger.info(String.format("This week blurbs count is %s.", thisWeek.size()));
        blurbs.addAll(lastWeekOnNotes);
        logger.info(String.format("Last week blurbs count is %s.", lastWeekOnNotes.size()));
        blurbs.addAll(pastWeeks);
        logger.info(String.format("Past weeks blurbs count is %s.", pastWeeks.size()));
        blurbs.addAll(community);
        logger.info(String.format("Community blurbs count is %s.", community.size()));

        final String listPrefix1 = "<li class=\"content\"><a href=\"#";
        final String listPrefix2 = "\" target=\"_self\">";
        final String listEnd = "</a></li>\n";
        final String horizontalRule = "\n<hr /><br>\n\n";
        StringBuilder s = new StringBuilder();
        // generate the content list
        int counter = 0;
        for (Blurb blurb : thisWeek) {
            s.append(listPrefix1).append(counter).append(listPrefix2).append(blurb.getTitle()).append(listEnd);
            counter++;
        }
        s.append("</ul>\n\n<span class=\"heading\">Last time appearing in Notes</span>\n<ul>");
        for (Blurb blurb : lastWeekOnNotes) {
            s.append(listPrefix1).append(counter).append(listPrefix2).append(blurb.getTitle()).append(listEnd);
            counter++;
        }
        s.append("</ul>\n\n<span class=\"heading\">Past weeks</span>\n<ul>");
        for (Blurb blurb : pastWeeks) {
            s.append(listPrefix1).append(counter).append(listPrefix2).append(blurb.getTitle()).append(listEnd);
            counter++;
        }
        s.append("</ul>\n\n<span class=\"heading\">Community</span>\n<ul>");
        for (Blurb blurb : community) {
            s.append(listPrefix1).append(counter).append(listPrefix2).append(blurb.getTitle()).append(listEnd);
            counter++;
        }
        s.append("</ul>\n\n").append(horizontalRule);
        // generate html for the main body
        // title tags
        String tPre1 = "<a id=\"";
        String tPre2 = "\" name=\"";
        String tPre3 = "\" style=\"text-decoration:none\"><span class="
                + "\"heading\">";
        String tEnd = "</span></a>\n";
        // content tags
        String cPre = "<p class=\"content\">";
        String cEnd = "</p>\n";
        counter = 0;
        for (Blurb blurb : blurbs) {
            String flierLinks = blurb.getFlierLinks();
            String content = HtmlUtils.convert(blurb.getContent());
            String tPre = tPre1 + counter + tPre2 + counter + tPre3;
            s.append(tPre).append(blurb.getTitle()).append(tEnd);
            if (flierLinks == null) s.append(cPre).append(content).append(cEnd);
            else s.append(cPre).append(content).append(" Flyer is at ").append(flierLinks)
                    .append(".").append(cEnd);
            // supports one picture flyer
            if (blurb.getImageUrl() != null) {
                String imgURL = blurb.getImageUrl();
                String pre = "<a href='" + imgURL
                        + "' target='_blank' title='Click to view'>";
                String end = "</a>";
                s.append("\n\n").append(pre).append("<img width =\"300\"; src=\"").append(imgURL)
                        .append("\">").append(end).append("\n<br><br>\n");
            }
            counter++;
        }
        s.append("<br><br>\n<!-- forMailChimpRight end -->");
        return s.toString();
    }

    private String generateLeftColumn(Calendar calendar) throws IOException {
        DateTime now = new DateTime(System.currentTimeMillis());
        DateTime twoMonthsFromNow = new DateTime(TimeUtils.getTwoMonthsFromNow(LocalDate.now()));
        Events events = calendar.events().list(Constants.GOOGLE.getProperty("bearfacts.gmail"))
                .setTimeMax(twoMonthsFromNow).setTimeMin(now).setOrderBy("startTime")
                .setSingleEvents(true).execute();
        Events officeNotesEvents = calendar.events()
                .list(Constants.GOOGLE.getProperty("officenotes.gmail"))
                .setTimeMax(twoMonthsFromNow).setTimeMin(now)
                .setOrderBy("startTime").setSingleEvents(true).execute();
        List<Event> items = events.getItems();
        items.addAll(officeNotesEvents.getItems());
        items.sort((e1, e2) -> {
            DateTime start1 = e1.getStart().getDateTime();
            DateTime start2 = e2.getStart().getDateTime();
            if (start1 == null) start1 = e1.getStart().getDate();
            if (start2 == null) start2 = e2.getStart().getDate();
            LocalDateTime s1 = LocalDateTime.ofEpochSecond(start1.getValue() / 1000,
                    0, ZoneOffset.ofHours(start1.getTimeZoneShift() / 60));
            LocalDateTime s2 = LocalDateTime.ofEpochSecond(start2.getValue() / 1000,
                    0, ZoneOffset.ofHours(start2.getTimeZoneShift() / 60));
            return s1.compareTo(s2);
        });

        DateTimeFormatter noMin = DateTimeFormatter.ofPattern("h a");
        DateTimeFormatter time = DateTimeFormatter.ofPattern("h:mm a");
        DateTimeFormatter dayOnly = DateTimeFormatter.ofPattern("MMM dd EEE");
        StringBuilder s = new StringBuilder();
        if (items.size() == 0) {
            logger.info("No upcoming events found.");
            return s.toString();
        }
        s.append("<!-- forMailChimpLeft start -->\n<span class=\"content\">\n");
        LocalDateTime lastStart = null;
        boolean allDayEvent, moreThanOneDay;
        for (Event event : items) {
            DateTime start = event.getStart().getDateTime();
            DateTime end = event.getEnd().getDateTime();
            // all day events, start == null
            if (start == null) {
                start = event.getStart().getDate();
                end = event.getEnd().getDate();
                allDayEvent = true;
            } else allDayEvent = false;
            /*
             * google API datetime.getValue returns milliseconds from epoch in the UTC time zone.
             * Do not have to use ZonedDateTime.now(ZoneId.of("America/Los_Angeles")))
             */
            LocalDateTime start1 = LocalDateTime.ofEpochSecond(
                    start.getValue() / 1000, 0,
                    ZoneOffset.ofHours(start.getTimeZoneShift() / 60));
            LocalDateTime end1 = LocalDateTime.ofEpochSecond(end.getValue() / 1000,
                    0, ZoneOffset.ofHours(start.getTimeZoneShift() / 60));

            if (start1.getYear() - LocalDateTime.now().getYear() > 1) {
                String msg = "Event start year is more than one year in future.";
                logger.severe(msg);
                throw new RuntimeException(msg);
            }

            // whether the event lasts for more than one day
            int days = end1.getDayOfYear() - start1.getDayOfYear();
            moreThanOneDay = days > 1 || (days < 0 && days > -364);

            // write a date and day
            if (lastStart == null || start1.getDayOfYear() > lastStart.getDayOfYear()
                    || start1.getYear() > lastStart.getYear()) {
                String day = start1.format(dayOnly);
                // whole day event's end date is actually the second day
                if (moreThanOneDay)
                    if (allDayEvent) day += " - " + end1.minusDays(1).format(dayOnly);
                    else day += " - " + end1.format(dayOnly);
                s.append("<br><span class=\"date\">").append(day).append("</span><br>\n");
            }

            // write an event entry and its time(optional)
            String entry = event.getSummary();
            if (!allDayEvent) {
                if (start1.getMinute() != 0) entry += " " + start1.format(time);
                else entry += " " + start1.format(noMin);
            }
            if (event.getLocation() != null) entry += " at " + event.getLocation();
            s.append(entry).append("<br>\n");

            // set lastStart to appropriate value
            if (allDayEvent && end1.getDayOfMonth() - start1.getDayOfMonth() > 1)
                lastStart = null;
            else lastStart = start1;
        }

        // write the calendar sticky section
        s.append("</span><br>").append(CALENDAR_STICKY).append("\n<!-- forMailChimpLeft end -->");
        return s.toString();
    }

    public String doAllCampaignJobs(Calendar calendar, List<Blurb> blurbs) throws IOException {
        logger.info("Start creating weeklyOfficeNote campaign ...");
        if (blurbs.size() == 0) return "No blurbs this week.";

        CampaignInfo campaignInfo = createCampaign("Office Notes "
                + TimeUtils.getComingMonday(LocalDate.now()).format(TimeUtils.CAMPAIGN_TITLE));
        setCampaignCotent(campaignInfo.id, PART1 + generateRightColumn(blurbs) + PART2
                + generateLeftColumn(calendar) + PART3);
        scheduleCampaign(campaignInfo.id, TimeUtils.getComingMonday6am(LocalDate.now()));
        logger.info("Finished creating weeklyOfficeNote campaign.");
        return campaignInfo.archive_url;
    }

    public String singleBlast(List<Blurb> blurbs) {
        logger.info("Start creating singleBlast campaign ...");
        String html = buildSingleBlast(blurbs);

        CampaignInfo campaignInfo = createCampaign("Office Notes Special Edition");
        setCampaignCotent(campaignInfo.id, String.format(SINGLE_BLAST, html));
        scheduleCampaign(campaignInfo.id, TimeUtils.getTheNextBusinessDay6am(LocalDate.now()));
        logger.info("Finished creating singleBlast campaign.");
        return campaignInfo.archive_url;
    }

    private String buildSingleBlast(List<Blurb> blurbs) {
        final String titlePrefix = "<h2><span style=\"color:#A52A2A\"><span class=\"heading\">";
        final String titleSuffix = "</span></span></h2>";
        final String contentPrefix = "<br />", contentSuffix = "<br /><br />";

        StringBuilder result = new StringBuilder();
        blurbs.forEach(b -> {
            result.append(titlePrefix).append(b.getTitle()).append(titleSuffix)
                    .append(contentPrefix).append(HtmlUtils.convert(b.getContent())).append(contentSuffix);
            if (b.getImageUrl() != null) {
                String pre = String.format("<a href='%s' target='_blank' title='Click to view'>", b.getImageUrl());
                String end = "</a>";
                result.append("<br><br>").append(pre).append("<img width =\"300\"; src=\"").append(b.getImageUrl())
                        .append("\">").append(end).append("\n<br><br>\n");
            }
        });
        return result.toString();
    }
}
