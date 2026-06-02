package com.expandtesting.hooks;

import com.expandtesting.base.BaseUi;
import com.expandtesting.drivers.GridDriverManager;
import com.expandtesting.utils.PerformanceLogger;
import com.expandtesting.utils.ScreenshotUtils;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Hooks {

    protected  final BaseUi baseUi = new BaseUi() {};

    @Before
    public void setup() {
        GridDriverManager.initDriver();
    }

    @After
    public void tearDown(Scenario scenario) {

        try {
            long pageLoadMs  = baseUi.getPageLoadTimeMs();
            long domReadyMs  = baseUi.getDomReadyTimeMs();
            String scenName  = scenario.getName();

            if (pageLoadMs > 0) {
                PerformanceLogger.logUiTiming(scenName, "pageLoad",  pageLoadMs, 3000);
            }
            if (domReadyMs > 0) {
                PerformanceLogger.logUiTiming(scenName, "domReady",  domReadyMs, 2000);
            }
        } catch (Exception ignored) {

        }


        if (scenario.isFailed()) {
            byte[] screenshot = ScreenshotUtils.captureScreenshot();
            if (screenshot.length > 0) {
                try {
                    scenario.attach(screenshot, "image/png",
                            "Failure Screenshot — " + scenario.getName());
                } catch (Exception e) {
                    System.out.println("[Hooks] scenario.attach failed: " + e.getMessage());
                }
                ScreenshotUtils.attachToAllure(
                        "Failure Screenshot — " + scenario.getName(), screenshot);
            } else {
                System.out.println("[Hooks] Screenshot empty for: " + scenario.getName());
            }
        }

        try {
            File perfLog = new File("target/performance-log.csv");
            if (perfLog.exists()) {
                Allure.addAttachment(
                        "Performance Trend Log",
                        "text/csv",
                        new FileInputStream(perfLog),
                        ".csv"
                );
            }
        } catch (Exception ignored) {}

        GridDriverManager.quitDriver();
    }
}
