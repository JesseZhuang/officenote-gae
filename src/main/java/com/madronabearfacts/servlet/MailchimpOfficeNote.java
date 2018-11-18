package com.madronabearfacts.servlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Creates a campaign and schedules to be sent out the coming Monday day at 6 am.
 */
@WebServlet(name = "MailchimpOfficeNote", value = "/admin/MailchimpOfficeNote")
public class MailchimpOfficeNote extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletHelper.prepLocalDatastore();
        response.setContentType("text/plain");
        String campaignUrl = ServletHelper.weeklyOfficeNote();
        response.getWriter().println(String.format("Created weeklyOfficeNote campaign at %s.", campaignUrl));
    }
}
