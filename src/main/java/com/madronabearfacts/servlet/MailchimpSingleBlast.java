package com.madronabearfacts.servlet;

import javax.mail.MessagingException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * <p>Creates a weeklyOfficeNote campaign and schedules to be sent out coming Monday at 6am. Idempotent because
 * will mark the blurbs as {@link com.madronabearfacts.entity.SingleBlast#SCHEDULED}.
 */
@WebServlet(name = "MailchimpSingleBlast", value = "/admin/MailchimpSingleBlast")
public class MailchimpSingleBlast extends HttpServlet {

    private final Logger logger = Logger.getLogger(MailchimpSingleBlast.class.getName());

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletHelper.prepLocalDatastore();
        response.setContentType("text/plain");
        String campaignUrl = null;
        try {
            campaignUrl = ServletHelper.singleBlast();
        } catch (MessagingException e) {
            logger.severe("Sending email confirmation for single blast failed.");
            e.printStackTrace();
        }
        response.getWriter().println(String.format("Created single blast campaign at %s.", campaignUrl));
    }
}
