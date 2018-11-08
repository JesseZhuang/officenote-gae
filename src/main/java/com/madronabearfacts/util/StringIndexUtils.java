package com.madronabearfacts.util;


public class StringIndexUtils {
    public static int findIndexOrThrowException(String text, String pattern) {
        int result = text.indexOf(pattern);
        if (result != -1) return result;
        else throw new RuntimeException("Cannot find " + pattern + " in text:\n" + text);
    }

    public static int findIndexOrThrowException(String text, String pattern, int startIndex) {
        int result = text.indexOf(pattern, startIndex);
        if (result != -1) return result;
        else throw new RuntimeException("Cannot find " + pattern + " in text:\n" + text + "\n"
                + "starting index " + startIndex);
    }
}
