package com.madronabearfacts.servlet;

import com.google.api.services.calendar.CalendarScopes;
import com.madronabearfacts.dao.BlurbDAO;
import com.madronabearfacts.entity.Blurb;
import com.madronabearfacts.helper.Constants;
import com.madronabearfacts.helper.GCalHelper;
import com.madronabearfacts.helper.GoogleAuthHelper;
import com.madronabearfacts.helper.MailchimpHelper;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Creates a mailchimp campaign and schedules to be sent out coming Monday at 6am.
 */
@WebServlet(name = "MailchimpServlet", value = "/admin/mailchimp")
public class MailchimpServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        BlurbDAO dao = new BlurbDAO();
        List<Blurb> blurbs = dao.getBlurbs(Constants.BLURB_ENTITY_KIND);
        response.setContentType("text/plain");
        String campaignUrl = new MailchimpHelper().doAllCampaignJobs(
                GCalHelper.getCalendarService(GoogleAuthHelper.getCredServiceAccountFromClassPath(
                        CalendarScopes.CALENDAR_READONLY)),
                blurbs);
        response.getWriter().println(String.format("Created mailchimp campaign at %s.", campaignUrl));
    }
}
