package com.madronabearfacts.servlet;

import com.google.appengine.api.utils.SystemProperty;

import javax.mail.MessagingException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Weekly cron job which does the following in sequence.
 */
@WebServlet(name = "WeeklyCronServlet", value = "/admin/cron/weekly")
public class WeeklyCronServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        ServletHelper.writeSchoolYearDates();

        Map<String, Integer> report = ServletHelper.updateArchiveDeleteBlurbs();
        int fetched = ServletHelper.fetchBlurbs();
        if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Development) {
            //racing at least in local development
            try {
                Thread.sleep(10000L);
                System.out.println("Sleeping 10s ...");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        String campaignUrl = ServletHelper.mailchimp();
        try {
            ServletHelper.sendEmailConfirmation(campaignUrl);
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        response.setContentType("text/plain");
        response.getWriter().println("Updated " + report.get(ServletHelper.TOTAL_UPDATED_COUNT) + " blurbs.\n"
                + "Archived " + report.get(ServletHelper.TO_BE_ARCHIVED_COUNT) + " blurbs.");
        response.getWriter().println("Fetched " + fetched + " blurbs.");
        response.getWriter().println(String.format("Created mailchimp campaign at %s.", campaignUrl));
    }
}
