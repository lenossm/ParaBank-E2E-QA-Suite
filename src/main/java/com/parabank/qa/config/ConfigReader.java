package com.parabank.qa.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {

    private static final Properties props = new Properties();

    static {
        try (InputStream stream = ConfigReader.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (stream == null) {
                throw new RuntimeException("config.properties not found on classpath");
            }
            props.load(stream);
        } catch (IOException e) {
            throw new RuntimeException("Could not load config.properties", e);
        }
    }

    private ConfigReader() {
    }

    public static String get(String key) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing config key: " + key);
        }
        return value.trim();
    }

    public static String get(String key, String defaultValue) {
        String value = props.getProperty(key);
        return (value == null || value.isBlank()) ? defaultValue : value.trim();
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public static double getDouble(String key) {
        return Double.parseDouble(get(key));
    }
}
