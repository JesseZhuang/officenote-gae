package com.madronabearfacts.util;

import com.google.common.io.CharStreams;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

public class FileUtils {

    private static final String CLASS_PATH_ERROR_MESSAGE = "Failed reading class path file %s.";
    private static final String FILE_PATH_ERROR_MESSAGE = "Failed reading file %s.";

    public static String readClassPathFileToString(String resourcePath) {
        try {
            InputStream in = FileUtils.class.getResourceAsStream(resourcePath);
            return CharStreams.toString(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(String.format(CLASS_PATH_ERROR_MESSAGE, resourcePath));
        }
    }

    public static Properties loadClassPathProperty(String resourcePath)  {
        try {
            Properties properties = new Properties();
            InputStream in = FileUtils.class.getResourceAsStream(resourcePath);
            properties.load(in);
            return properties;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(String.format(CLASS_PATH_ERROR_MESSAGE, resourcePath));
        }
    }

    @SuppressWarnings("unused")
    public static Properties loadFilePathProperty(String resourcePath)  {
        try {
            Properties properties = new Properties();
            InputStream in = Files.newInputStream(Paths.get(resourcePath), StandardOpenOption.READ);
            properties.load(in);
            return properties;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(String.format(FILE_PATH_ERROR_MESSAGE, resourcePath));
        }
    }

}
