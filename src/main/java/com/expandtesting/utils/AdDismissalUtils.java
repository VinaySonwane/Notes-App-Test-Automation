package com.expandtesting.utils;

import com.expandtesting.drivers.GridDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;

public class AdDismissalUtils {


    public static void dismissAds() {
        WebDriver driver = GridDriverManager.getDriver();
        try {

            String currentUrl = driver.getCurrentUrl();
            if (currentUrl != null && currentUrl.contains("google_vignette")) {
                ((JavascriptExecutor) driver).executeScript(
                        "if (window.location.hash && window.location.hash.includes('google_vignette')) {"
                                + "  history.replaceState(null, '', window.location.pathname + window.location.search);"
                                + "}"
                );

                new WebDriverWait(driver, Duration.ofSeconds(3))
                        .until(d -> !d.getCurrentUrl().contains("google_vignette"));
            }

            // ── Layer 2: click a visible "Close" button in the main document ──
            By closeButton = By.xpath(
                    "//*[normalize-space(text())='Close' or normalize-space(text())='✕' "
                            + "or normalize-space(text())='×' or @aria-label='Close' "
                            + "or @title='Close' or contains(@class,'close-button') "
                            + "or contains(@id,'dismiss') or contains(@id,'close')]"
            );
            List<WebElement> closeBtns = driver.findElements(closeButton);
            for (WebElement btn : closeBtns) {
                try {
                    if (btn.isDisplayed()) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
                        final WebElement clickedBtn = btn;
                        new WebDriverWait(driver, Duration.ofSeconds(3))
                                .until(ExpectedConditions.or(
                                        ExpectedConditions.stalenessOf(clickedBtn),
                                        ExpectedConditions.invisibilityOf(clickedBtn)
                                ));
                        break;
                    }
                } catch (Exception ignored) {}
            }

            // ── Layer 3: look inside iframes for ad close buttons ──
            List<WebElement> iframes = driver.findElements(By.tagName("iframe"));
            for (WebElement iframe : iframes) {
                try {
                    if (!iframe.isDisplayed()) continue;
                    driver.switchTo().frame(iframe);
                    List<WebElement> innerClose = driver.findElements(closeButton);
                    for (WebElement btn : innerClose) {
                        try {
                            if (btn.isDisplayed()) {
                                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);

                                final WebElement clickedInner = btn;
                                new WebDriverWait(driver, Duration.ofSeconds(2))
                                        .until(ExpectedConditions.or(
                                                ExpectedConditions.stalenessOf(clickedInner),
                                                ExpectedConditions.invisibilityOf(clickedInner)
                                        ));
                                break;
                            }
                        } catch (Exception ignored) {}
                    }
                    driver.switchTo().defaultContent();
                } catch (Exception ignored) {
                    // Always return to main frame even if iframe access fails.
                    try { driver.switchTo().defaultContent(); } catch (Exception e2) {}
                }
            }

        } catch (Exception ignored) {
            // dismissAds is best-effort — never let it break the test.
            try { GridDriverManager.getDriver().switchTo().defaultContent(); } catch (Exception e2) {}
        }
    }



    public static void safeNavigate(String url) {
        WebDriver driver = GridDriverManager.getDriver();

        String cleanUrl = url.replaceAll("#.*$", "");
        driver.get(cleanUrl);
        try {
            new WebDriverWait(driver, Duration.ofSeconds(20))
                    .until(d -> ((JavascriptExecutor) d)
                            .executeScript("return document.readyState").equals("complete"));
        } catch (Exception ignored) {}
        // Dismiss any ad that appeared immediately after page load.
        dismissAds();
    }
}
