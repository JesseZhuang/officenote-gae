package com.madronabearfacts.helper;

import com.madronabearfacts.entity.Eflier;
import com.madronabearfacts.util.TimeUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EflierCrawler {

    private static final String START_IDENTIFIER = "<li";
    private static final String END_IDENTIFIER = "</li>";
    private static final String URL_IDENTIFIER = "<a href=\"";

    public List<Eflier> crawlAllEfliers() {
        List<Eflier> efilers = new ArrayList<>();
        List<String> pageUrls = Constants.EFLIER_URLS;
        for (String pageUrl : pageUrls) {
            efilers.addAll(crawlEflierOnPage(pageUrl));
        }
        return efilers;
    }

    private List<Eflier> crawlEflierOnPage(String pageUrl) {
        List<Eflier> efilers = new ArrayList<>();
        String eflierSection = getEflierSection(pageUrl);
        int startIndex = 0, endIndex;
        LocalDate lastMonday = TimeUtils.getLastMonday();
        while (startIndex >= 0) {
            endIndex = findIndexOrThrowException(eflierSection, END_IDENTIFIER, startIndex);
            String eflierLine = getOneEflierLine(eflierSection, startIndex, endIndex);

            String postedDateString;
            try {
                // return null if date format wrong, cannot parse
                postedDateString = getPostedDate(eflierLine);
            } catch (Exception e) {
                // 2018/01/13 one eflier line with just one space in there, no other content
                startIndex = eflierSection.indexOf(START_IDENTIFIER, endIndex);
                continue;
            }

            LocalDate postedDate = TimeUtils.parseDate(postedDateString);
            if (postedDate != null && postedDate.isBefore(lastMonday)) break;

            efilers.add(new Eflier(getEflierTitle(eflierLine), getEflierDownloadUrl(eflierLine),
                    Optional.ofNullable(postedDateString)));
            startIndex = eflierSection.indexOf(START_IDENTIFIER, endIndex);
        }

        return efilers;
    }

    private String getEflierSection(String pageUrl) {
        String htmlContent = getHtmlFromUrl(pageUrl);
        int startIndex = getListStartIndex(htmlContent);
        int endIndex = getListEndIndex(htmlContent, startIndex);
        return htmlContent.substring(startIndex, endIndex);
    }

    private String getOneEflierLine(String fliers, int startIndex, int endIndex) {
        final String closeTag = ">";
        startIndex = findIndexOrThrowException(fliers, START_IDENTIFIER, startIndex);
        return fliers.substring(findIndexOrThrowException(fliers, closeTag, startIndex) + closeTag.length(), endIndex);
    }

    private String getEflierTitle(String eflierLine) {
        final String plainText = eflierLine.substring(0, findIndexOrThrowException(eflierLine, URL_IDENTIFIER));
        String downloadUrlText;
        final String urlTitleIdentifier = "title=\"";
        int startIndex = findIndexOrThrowException(eflierLine, urlTitleIdentifier) + urlTitleIdentifier.length();
        int endIndex = findIndexOrThrowException(eflierLine, "\"", startIndex);
        downloadUrlText = eflierLine.substring(startIndex, endIndex);
        return plainText + downloadUrlText;
    }

    private String getEflierDownloadUrl(String eflierLine) {
        int startIndex = findIndexOrThrowException(eflierLine, URL_IDENTIFIER) + URL_IDENTIFIER.length();
        int endIndex = findIndexOrThrowException(eflierLine, "\"", startIndex);
        String downloadUrl = eflierLine.substring(startIndex, endIndex).replace(" ", "%20");
        if (downloadUrl.startsWith("http")) return downloadUrl;
        return Constants.ESD_DOMAIN + downloadUrl;
    }

    private String getPostedDate(String eflierLine) {
        int index = findIndexOrThrowException(eflierLine, "posted");
        String datePart = eflierLine.substring(index);
        // assumption is month/date/year format
        Matcher matcher = Pattern.compile("\\d+/\\d+/\\d+").matcher(datePart);
        if (matcher.find()) return matcher.group();
        else return null;
    }

    private int getListStartIndex(String htmlContent) {
        final String listStartIdentifier = "<ul>    <li";
        return findIndexOrThrowException(htmlContent, listStartIdentifier);
    }

    private int getListEndIndex(String htmlContent, int listStartIndex) {
        final String listEndIdentifier = "</li></ul>";
        return findIndexOrThrowException(htmlContent, listEndIdentifier, listStartIndex) + END_IDENTIFIER.length();
    }

    private int findIndexOrThrowException(String text, String pattern) {
        int result = text.indexOf(pattern);
        if (result != -1) {
            return result;
        } else {
            throw new RuntimeException("Cannot find " + pattern + " in text:\n" + text);
        }
    }

    private int findIndexOrThrowException(String text, String pattern, int startIndex) {
        int result = text.indexOf(pattern, startIndex);
        if (result != -1) {
            return result;
        } else {
            throw new RuntimeException("Cannot find " + pattern + " in text:\n" + text);
        }
    }

    private String getHtmlFromUrl(String urlAddress) {
        URL urlObject = null;
        try {
            urlObject = new URL(urlAddress);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        URLConnection urlConnection = null;
        try {
            urlConnection = urlObject.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 " +
                "(KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");

        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(urlConnection.getInputStream(), "UTF-8"))) {
            String inputLine;
            StringBuilder stringBuilder = new StringBuilder();
            while ((inputLine = bufferedReader.readLine()) != null) {
                stringBuilder.append(inputLine);
            }

            return stringBuilder.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("crawl html failed");
    }

    public static void main(String[] args) {
        EflierCrawler crawler = new EflierCrawler();
        for (String pageUrl : Constants.EFLIER_URLS) {
//            System.out.println("[debug]: for pageUrl "+ pageUrl);
//
            System.out.println(crawler.getEflierSection(pageUrl));
            System.out.println("----------");
            for (Eflier flier : crawler.crawlEflierOnPage(pageUrl)) System.out.println(flier);
            System.out.println("------");
        }
//        System.out.println(crawler.crawlAllEfliers());

//        System.out.println("tes".indexOf("t"));
//        System.out.println("tes".indexOf("t", 0));

    }
}
