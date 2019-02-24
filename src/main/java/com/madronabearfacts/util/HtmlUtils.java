package com.madronabearfacts.util;

/**
 * Utility class used to wrap web urls, email addresses, and unordered lists with html that
 * are commonly used in the school newsletter.
 * <p>
 * Use {@link #convert(String)} method to convert simple text to wrapped html
 * code. For example,
 * <p>
 * "http://www.example.com" will be converted to
 * <a href="http://www.example.com" target = "_blank">this link</a>.<br>
 * "example_email@example.com" will be converted to
 * <a href="mailto:example_email@example.com" target = "_blank">this email</a>.
 * <p>
 * unordered list like below:<br>
 * * list item 1<br>
 * * list item 2<br>
 * <p>
 * will be converted to:<br>
 * <ul>
 * <li>list item 1
 * <li>list item 2
 * </ul>
 */
public class HtmlUtils {
    // *: 0 or more; ?: 0 or 1; + 1 or more; re{n}: exactly n repeats of re
    // [] matches any single character in brackets
    public static final String URL_PATTERN = "\\b(https?|ftp|file)://[-a-zA-Z0-9+()&@#/%?=~_|!:,'.;]+" +
            "[-a-zA-Z0-9+&@%#/=~_|]+";
    /*
     * "(http://|https://|HTTP://|HTTPS://)+" +
     * "(www.)?([a-zA-Z0-9-_]+)[.]([a-zA-Z0-9]+[.])*([a-zA-Z0-9]+)" +
     * "(/[a-zA-Z0-9-_]+)*.([a-z]+)?"; does not work well.
     */
    public static final String URL_REPL = "<a href=\"$0\" target = \"_blank\">"
            + "this link</a>";
    public static final String EMAIL_PATTERN = "\\b[\\w.%-]+@[-.\\w]+\\."
            + "[A-Za-z]{2,4}\\b";
    public static final String EMAIL_REPL = "<a href=\"mailto:$0\" target = \"_"
            + "blank\">this email</a>";

    private static String convertURL(String str) {
        // some people does not add http:// to the link address
        String w = "www";
        String strLowerCase = str.toLowerCase();
        if (strLowerCase.contains(w)) {
            int ind = strLowerCase.indexOf(w);
            while (ind < str.length() && ind >= 0) {
                if (str.charAt(ind - 1) != '/') {
                    str = str.substring(0, ind) + "http://" + str.substring(ind);
                    strLowerCase = strLowerCase.substring(0, ind) + "http://" + strLowerCase.substring(ind);
                }

                ind = strLowerCase.indexOf(w, ind + "http://".length());
            }
        }
        str = str.replaceAll(URL_PATTERN, URL_REPL);
        return str;
    }

    private static String convertEmail(String str) {
        str = str.replaceAll(EMAIL_PATTERN, EMAIL_REPL);
        return str;
    }

    public static String convert(String str) {
        if (str.contains("•")) str = convertList(str);
        if (str.contains("@")) str = convertEmail(str);
        str = convertURL(str);
        return str;
    }

    private static String convertList(String str) {
        // the bullet point I am using is \u2022
        // separate patterns for e-flier list item which has hyper link and
        // other symbols
        // String listPattern_eflier =
        // "((<br\\s+/>)?(•[\\w\\s&-]+((https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~
        // _|!:,.;]*[-a-zA-Z"
        // + "0-9+&@#/%=~_|])+[\\w:()&-,A-Za-z/0-9\\s]+(<br\\s+/>)*))+";
        // String listPattern = "(<br\\s+/>)?(•[\\w.\\s]+(<br\\s+/>)*)+";
        String listPattern = "(<br\\s+/>)?(•[\\w.\\s&-:\"()0-9/(https?|ftp|file)"
                + "://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]"
                + "*[-a-zA-Z0-9+&@#/\"%=~_|–,])]+(<br\\s+/>)*)+";
        String listWrap = "<ul>$0</ul>";
        // str = str.replaceAll(listPattern_eflier,
        // listWrap).replaceAll("(<br\\s+/>)*•", "</li><li>")
        str = str.replaceAll(listPattern, listWrap)
                .replaceAll("(<br\\s+/>)*•", "</li><li>")
                .replaceAll("<ul>(<br\\s+/>)*</li>", "</p><p><ul>")
                .replaceAll("(<br\\s+/>)?</ul>",
                        "</li></ul><p class=\"content\">")
                .replaceAll("<p\\sclass=\"content\">\\z", "");
        return str;
    }

    public static String removeHtml(String str) {
        return str.replace("<br />", System.lineSeparator()).replace("&amp;", "&")
                .replace("&#039;", "'").replace("&quot;", "\"")
                .replaceAll("<(\"[^\"]*\"|'[^']*'|[^'\">])*>", "");
    }

    private static void testRE() {
        // String s = "one space: , two space:  , 3 spaces:   ;";
        // System.out.println(s);
        // s = s.replaceAll("\\s{2,}", " ");
        //
        // System.out.println(s.replaceFirst("space", "spc"));

        String sa = " bla bla at www.bearfacts.com/man/ui/noname.pdf\n"
                + "bla bla at www.bearfacts.com/noname.pdf\n"
                + "bla bla at WWW.bearfacts.com/noname.pdf\n"
                + "bla bla at WWW.bearfacts.com/noname.pdf.\n";

        System.out.println(HtmlUtils.convertURL(sa));

        String sb = "mandante at dmendoza0715@gmail.com or Gretchen Gruender\n"
                + " at gretchen_gruender@yahoo.com\n madronatickets@gmail.com\n "
                + "with any questions at madronastem@gmail.com and at "
                + "madronaartaucti​on@gmail.com aapple@grile.com.\n";

        System.out.println(HtmlUtils.convertEmail(sb));
        System.out.println("give madronaartauction@gmail.com"
                .replaceAll(EMAIL_PATTERN, EMAIL_REPL));
        System.out.println("give madronaartaucti​on@gmail.com"
                .replaceAll(EMAIL_PATTERN, EMAIL_REPL));

        char[] emailA = "madronaartauction@gmail.com".toCharArray();
        char[] emailB = "madronaartaucti​on@gmail.com".toCharArray();

        // unicode 8203 zero width space...
        for (int i = 0; i < emailA.length; i++) {
            System.out.println(emailA[i] + " " + emailB[i] + ":equal? :"
                    + (emailA[i] == emailB[i]));
            if (emailA[i] != emailB[i])
                System.out.printf("%d, %d\n", (int) emailA[i], (int) emailB[i]);
        }

        String p2lists = "paragraph1<br />list1\n"
                + "<br />•item1 of list1<br />•item2 of list1\n"
                + "<br />list2<br />•item1<br />•item2<br />paragraph2\n"
                + "<br />list3<br />•item1<br />•item2";

        String plists = "list from Microsoft word, also test multiple line\n"
                + " returns at the end of the blurb content.\n"
                + "<br />•	Test1 flier at http://www.a.com man flier : (grades)"
                + " <br />•	Test2 flier at http://www.google.com flier : "
                + "(grades) <br /> new paragraph";

        String plists2 = "<br />•	Test1<br />•	Test2 <br /> new paragraph";

        System.out.println(HtmlUtils.convertList(p2lists));
        System.out.println();
        System.out.println(HtmlUtils.convertList(plists));
        System.out.println();
        System.out.println(convertList(plists2));

        System.out.println("test".equals("test"));
        System.out.println("test" == "test");
        System.out.println("\u2022" == "•");
        System.out.println(String.format("\\u%04x", (int) '·'));
        System.out.println(String.format("\\u%04x", (int) '•'));

        String urlWithSingleQuote = "http://www.edmonds.wednet.edu/UserFiles/Servers/Server_306670/File/Community/" +
                "Community%20eFliers/Community%20Activites/MTH%20Boosters%20Breakfast%20in%20Santa's%20Workshop.pdf";

        System.out.println(urlWithSingleQuote.replaceAll(URL_PATTERN, URL_REPL));

    }

    public static void main(String[] args) {
        testRE();

    }
}
