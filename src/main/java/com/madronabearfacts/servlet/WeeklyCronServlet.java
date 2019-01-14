package com.madronabearfacts.servlet;

import com.madronabearfacts.helper.Constants;

import javax.mail.MessagingException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>First check for holidays, last update blurbs time. Weekly cron job: put together the weekly office note,
 * then update, archive, delete blurbs as necessary, then send email confirmation.
 */
@WebServlet(name = "WeeklyCronServlet", value = "/admin/cron/weekly")
public class WeeklyCronServlet extends HttpServlet {

    private final Logger logger = Logger.getLogger(WeeklyCronServlet.class.getName());

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        ServletHelper.prepLocalDatastore();
        if (!ServletHelper.shouldExecuteCronWeekly()) return;

        String campaignUrl = ServletHelper.weeklyOfficeNote();
        ServletHelper.updateArchiveDeleteBlurbs();
        sendEmail(campaignUrl);

        response.setContentType("text/plain");
        response.getWriter().println(String.format("Created weeklyOfficeNote campaign at %s.", campaignUrl));
    }

    @Deprecated
    private void sleepForLocal() {
        if (Constants.isLocalDev) {
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
            if (Constants.isLocalDev)
                ServletHelper.sendConfirmationLocal(campaignUrl);
            else ServletHelper.sendMITChairConfirmation(campaignUrl);
        } catch (MessagingException e) {
            logger.log(Level.SEVERE, "exception ", e);
        }
    }
}
