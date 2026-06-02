package com.expandtesting.drivers;

import com.expandtesting.config.ConfigReader;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GridDriverManager {
    private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    public static void initDriver() {
        try {
            ChromeOptions options = new ChromeOptions();

            options.addArguments("--disable-extensions");
            options.addArguments("--disable-infobars");
            options.addArguments("--disable-notifications");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-gpu");
            options.addArguments("--host-rules=MAP googleads.g.doubleclick.net 127.0.0.1,"
                    + "MAP pagead2.googlesyndication.com 127.0.0.1,"
                    + "MAP adservice.google.com 127.0.0.1,"
                    + "MAP tpc.googlesyndication.com 127.0.0.1");
            options.setExperimentalOption("excludeSwitches",
                    Arrays.asList("enable-automation"));
            options.setExperimentalOption("useAutomationExtension", false);

            Map<String, Object> prefs = new HashMap<>();
            prefs.put("profile.default_content_setting_values.ads", 2);
            prefs.put("profile.default_content_setting_values.popups", 2);
            prefs.put("profile.default_content_setting_values.notifications", 2);
            options.setExperimentalOption("prefs", prefs);


            URL gridUrl = new URL(ConfigReader.getProperty("grid.url"));
            driver.set(new RemoteWebDriver(gridUrl, options));

            driver.get().manage().window().maximize();
            driver.get().manage().timeouts().pageLoadTimeout(java.time.Duration.ofSeconds(30));
        } catch (Exception e) {
            System.err.println("[GridDriverManager] Could not connect to Selenium Grid: " + e.getMessage());
            throw new RuntimeException("Could not connect to Grid: " + e.getMessage());
        }
    }

    public static WebDriver getDriver() {
        return driver.get();
    }

    public static void quitDriver() {
        if (driver.get() != null) {
            driver.get().quit();
            driver.remove();
        }
    }
}