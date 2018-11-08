package com.madronabearfacts.helper;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.StringUtils;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.madronabearfacts.entity.Blurb;
import com.madronabearfacts.util.StringIndexUtils;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * <p>Read email messages from madrona officenotes email. Google app engine has its own version of javax.mail which
 * conflicts with javax.mail 1.4.7. Regular gmail account (non G Suite) can not delegate to service account. This
 * implementation uses OAUTH 2.0 which requires to deploy the credential key json and stored credential files together
 * with the app engine application.
 * <p>For maven build, after login for oauthm, manually copy the "gmail_stored_cred" folder to
 * "target/officenotes-1.0-SNAPSHOT" folder and it will be included into the war file.
 */
public class GmailHelper {

    private final static Logger LOGGER = Logger.getLogger(GmailHelper.class.getName());
    private final static String CONTENT_PREFIX = "<font style=\"font-family: sans-serif; font-size:12px;\">";
    private final static String CONTENT_SUFFIX = "</font>";

    public static List<String> getMessages(Gmail service) throws IOException {
        // Print the labels in the user's account.
        String user = "me";
        ListMessagesResponse listResponse = service.users().messages().list(user)
                .setQ("subject:\"Madronabearfacts.com Article Submission Form\" label:inbox ").execute();
        List<Message> messages = listResponse.getMessages();
        List<String> htmlMessages = new ArrayList<>();
        if (messages.isEmpty()) {
            LOGGER.info("No madrona office note submission messages found.");
        } else {
            LOGGER.info("Messages count: " + messages.size());
            for (Message message : messages) {
                Message m = service.users().messages().get(user, message.getId()).execute();
                htmlMessages.add(StringUtils.newStringUtf8(
                        m.getPayload().getBody().decodeData()));
            }
        }
        return htmlMessages;
    }

    public static boolean sendMessage(Gmail service, String subject, String bodyHtml)
            throws MessagingException, IOException {
        Message message = createMessageWithEmail(
                createEmail(Constants.GOOGLE.getProperty("officenotes.gmail"),
                        Constants.GOOGLE.getProperty("mitchair.email"),
                        Constants.GOOGLE.getProperty("jesse.email"),
                        subject, bodyHtml
                ));

        return service.users().messages().send(Constants.GOOGLE.getProperty("officenotes.gmail"), message)
                .execute().getLabelIds().contains("SENT");
    }

    private static MimeMessage createEmail(String from, String to, String bcc, String subject, String bodyHtml)
            throws MessagingException {
        MimeMessage email = new MimeMessage(Session.getDefaultInstance(new Properties(), null));
        email.setFrom(new InternetAddress(from));
        email.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(to));
        email.addRecipients(MimeMessage.RecipientType.BCC, bcc);
        email.setSubject(subject);
        email.setContent(bodyHtml, "text/html; charset=utf-8");
        return email;
    }

    private static Message createMessageWithEmail(MimeMessage emailContent) throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);

        return new Message()
                .setRaw(Base64.encodeBase64URLSafeString(buffer.toByteArray()));
    }

    private static List<Blurb> getBlurbs(List<String> htmlMessages) throws ParseException {
        // precedes every information's key and value
        final String titleKey = "<strong>Article Title:</strong>";
        final String numWeeksKey = "How long should article be printed?";
        final String contentKey = "<strong>Article Contents:</strong>";
        final String flierLinksKey = "Upload Photo, Flyer, etc:";
        final String startDateKey = "Start Date:";

        List<Blurb> blurbs = new ArrayList<>();

        Date fetchDate = new Date();

        for (String message : htmlMessages) {
            String title = findInfo(message, titleKey, CONTENT_SUFFIX);
            int numWeeks = Integer.parseInt(findInfo(message, numWeeksKey, " Week"));
            String content = findInfo(message, contentKey, CONTENT_SUFFIX);
            // remove line returns, extra <br>s, extra white spaces. • word bullet sign
            content = content.replace("\r\n", "")
                    .replaceAll("((<br />)\\s*){2,}", "<br />")
                    .replaceAll("\\s{2,}", " ").replace("·", "•");
            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
            Date startDate = format.parse(findInfo(message, startDateKey, CONTENT_SUFFIX));

            // if flyer link is available, get and add the blurb
            if (message.contains(flierLinksKey)) {
                String flierLinks = findInfo(message, flierLinksKey, "<ul><li>", "</li></ul>")
                        .replaceAll("\r\n", "")
                        .replace("</li><li>", " and ");
                flierLinks = cutLongFileName(flierLinks);

                blurbs.add(Blurb.builder()
                        .title(title).numWeeks(numWeeks).curWeek(1).content(content)
                        .startDate(startDate).fetchDate(fetchDate)
                        .flierLinks(flierLinks).imageUrl(getImageUrl(flierLinks))
                        .build());
            } else
                blurbs.add(Blurb.builder()
                        .title(title).numWeeks(numWeeks).curWeek(1).content(content)
                        .startDate(startDate).fetchDate(fetchDate)
                        .build());
        }
        return blurbs;
    }

    private static String findInfo(String message, String toFind, String suffix) {
        return findInfo(message, toFind, CONTENT_PREFIX, suffix);
    }

    private static String findInfo(String message, String toFind, String prefix, String suffix) {
        int startInd = message.indexOf(toFind);
        startInd = StringIndexUtils.findIndexOrThrowException(message, prefix, startInd)
                + prefix.length();
        return message.substring(startInd, message.indexOf(suffix, startInd));
    }

    private static String cutLongFileName(String flierLinks) {
        int ind = -1;
        String result = flierLinks;
        while ((ind = flierLinks.indexOf("http", ind + 1)) > 0) {
            int filenameStart = flierLinks.indexOf(">", ind) + 1;
            int filenameDot = flierLinks.indexOf(".", filenameStart);
            // if filename longer than 35, keep last 10 chars
            if (filenameDot - filenameStart > 35) {
                result = flierLinks.substring(0, filenameStart) + flierLinks.substring(filenameDot - 10);
            }
        }
        return result;
    }

    private static String getImageUrl(String flierLinks) {
        int ind = -1;
        while ((ind = flierLinks.indexOf("http", ind + 1)) > 0) {
            String flyerURL = flierLinks.substring(ind, flierLinks.indexOf("'", ind));
            if (flyerURL.matches("([^\\s]+(\\.(?i)(jpg|png|gif|bmp)).*)")) {
                return flyerURL.replace("&amp;", "&");
            }
        }
        return null;
    }

    public static List<Blurb> getBlurbs() {
        try {
            return getBlurbs(getMessages(GoogleAuthHelper.getGmailService()));
        } catch (ParseException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Exception when fetch blurbs.");
        }
    }

    public static void main(String[] args) throws IOException, ParseException, MessagingException {
//        System.out.println(getMessages(GoogleAuthHelper.getGmailServiceFromFilePath()));
//        List<Blurb> blurbs = getBlurbs(getMessages(GoogleAuthHelper.getGmailServiceFromFilePath()));
        List<Blurb> blurbs = getBlurbs(getMessages(GoogleAuthHelper.getGmailService()));
        for (Blurb blurb : blurbs) {
            System.out.println(blurb);
        }

        String longFlierLinkname = "<a href='https://www.madronabearfacts.com/index.php?gf-download=" +
                "2018%2F11%2FIMG_8930.jpg&amp;form-id=1&amp;field-id=21&amp;hash=7ad4fac922cbd1a2f1c715518" +
                "e5590adf6b151959aa7318fff3de1fa50a4ffa6' target='_blank' title='Click to view'>" +
                "verylongfilenameverylongfilenameverylongfilenameverylongfilenameverylongfilenameIMG_8930.jpg</a>";
        System.out.println(cutLongFileName(longFlierLinkname));
        System.out.println(getImageUrl(longFlierLinkname));
    }
}
