package com.madronabearfacts.servlet;

import com.google.api.services.calendar.CalendarScopes;
import com.madronabearfacts.dao.BlurbDAO;
import com.madronabearfacts.entity.Blurb;
import com.madronabearfacts.helper.GCalHelper;
import com.madronabearfacts.helper.GoogleAuthHelper;
import com.madronabearfacts.helper.MailchimpHelper;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static com.madronabearfacts.dao.BlurbDAO.ACTIVE_BLURB_KIND;

/**
 * Creates a single blast campaign and schedules to be sent out the coming Monday day at 6 am.
 */
@WebServlet(name = "MailchimpOfficeNote", value = "/admin/MailchimpOfficeNote")
public class MailchimpOfficeNote extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletHelper.prepLocalDatastore();
        BlurbDAO dao = new BlurbDAO();
        List<Blurb> blurbs = dao.getBlurbs(ACTIVE_BLURB_KIND);
        response.setContentType("text/plain");
        String campaignUrl = new MailchimpHelper().doAllCampaignJobs(
                GCalHelper.getCalendarService(GoogleAuthHelper.getCredServiceAccountFromClassPath(
                        CalendarScopes.CALENDAR_READONLY)),
                blurbs);
        response.getWriter().println(String.format("Created weeklyOfficeNote campaign at %s.", campaignUrl));
    }
}
