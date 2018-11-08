package com.madronabearfacts.servlet;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.utils.SystemProperty;

import javax.mail.MessagingException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Weekly cron job which does the following in sequence.
 */
@WebServlet(name = "WeeklyCronServlet", value = "/admin/cron/weekly")
public class WeeklyCronServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        ServletHelper.prepLocalDatastore();

        List<Key> stayOn = ServletHelper.updateArchiveDeleteBlurbs();
//        sleepForLocal();
        List<Key> thisWeek = ServletHelper.fetchBlurbs();
        List<Key> active = new ArrayList<>();
        active.addAll(stayOn);
        active.addAll(thisWeek);
//        sleepForLocal();
        String campaignUrl = ServletHelper.mailchimp(active);
        sendEmail(campaignUrl);

        response.setContentType("text/plain");
        response.getWriter().println(String.format("Created mailchimp campaign at %s.", campaignUrl));
    }

    private void sleepForLocal() {
        if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Development) {
            //issue for local development, chrome(canary)'s fault, multiple requests
            Object lock = new Object();
            synchronized (lock) {
                try {
                    System.out.println("Sleeping 60s ...");
                    lock.wait(60000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendEmail(String campaignUrl) throws IOException {
        try {
            if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Development)
                ServletHelper.sendEmailConfirmationLocal(campaignUrl);
            else ServletHelper.sendEmailConfirmation(campaignUrl);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
