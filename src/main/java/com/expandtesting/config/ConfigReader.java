package com.expandtesting.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {

    protected static final Properties properties = new Properties();

    static {

        try (FileInputStream base =
                     new FileInputStream("src/test/resources/config.properties")) {
            properties.load(base);
        } catch (IOException e) {
            throw new RuntimeException(
                    "config.properties not found — expected at src/test/resources/config.properties", e);
        }
    }


    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}
