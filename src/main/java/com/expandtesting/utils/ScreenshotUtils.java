package com.expandtesting.utils;

import com.expandtesting.drivers.GridDriverManager;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.ByteArrayInputStream;

public class ScreenshotUtils {

    public static byte[] captureScreenshot() {
        WebDriver driver = GridDriverManager.getDriver();
        if (driver == null) return new byte[0];
        try {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        } catch (Exception e) {
            System.out.println("[ScreenshotUtils] Could not capture screenshot: " + e.getMessage());
            return new byte[0];
        }
    }
    
    public static void attachToAllure(String name, byte[] screenshotBytes) {
        if (screenshotBytes == null || screenshotBytes.length == 0) return;
        try {
            Allure.addAttachment(name, "image/png",
                    new ByteArrayInputStream(screenshotBytes), "png");
        } catch (Exception e) {
            System.out.println("[ScreenshotUtils] Allure.addAttachment failed: " + e.getMessage());
        }
    }
}
