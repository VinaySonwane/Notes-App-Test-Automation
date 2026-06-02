package com.expandtesting.base;

import com.expandtesting.drivers.GridDriverManager;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

public class BaseUi {

    protected WebDriver getDriver() {
        return GridDriverManager.getDriver();
    }

    public long getPageLoadTimeMs() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) getDriver();
            Object loadEnd   = js.executeScript("return window.performance.timing.loadEventEnd");
            Object navStart  = js.executeScript("return window.performance.timing.navigationStart");
            if (loadEnd == null || navStart == null) return -1;
            long end   = Long.parseLong(loadEnd.toString());
            long start = Long.parseLong(navStart.toString());
            return (end > 0 && start > 0) ? end - start : -1;
        } catch (Exception e) {
            return -1;
        }
    }

    public long getDomReadyTimeMs() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) getDriver();
            Object domReady  = js.executeScript("return window.performance.timing.domContentLoadedEventEnd");
            Object navStart  = js.executeScript("return window.performance.timing.navigationStart");
            if (domReady == null || navStart == null) return -1;
            long end   = Long.parseLong(domReady.toString());
            long start = Long.parseLong(navStart.toString());
            return (end > 0 && start > 0) ? end - start : -1;
        } catch (Exception e) {
            return -1;
        }
    }
}
