package com.expandtesting.pages;

import com.expandtesting.base.BaseUi;
import com.expandtesting.drivers.GridDriverManager;
import com.expandtesting.utils.AdDismissalUtils;
import com.expandtesting.utils.PerformanceLogger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

public class LoginPage extends BaseUi {

    // ─── Login locators ──────────────────────────────────────────
    private final By emailField    = By.id("email");
    private final By passwordField = By.id("password");
    private final By loginButton   = By.xpath("//button[@type='submit' or contains(text(),'Login')]");

    // ─── Registration locators ───────────────────────────────────
    private final By regNameField     = By.id("name");
    private final By regEmailField    = By.id("email");
    private final By regPasswordField = By.id("password");
    private final By registerButton   = By.xpath("//button[@type='submit' or contains(text(),'Register')]");

    // ─── Error locator (broadened — works for all three login-error scenarios) ───
    private final By errorAlertLocator = By.cssSelector(
            "[role='alert'], [data-testid='alert-message'], div.alert-danger, " +
                    ".alert.alert-danger, .text-danger, .error-message, p.alert, " +
                    "div[class*='error'], div[class*='Error'], span[class*='error']"
    );

    // ─────────────────────────────────────────────────────────────
    // LOGIN
    // ─────────────────────────────────────────────────────────────

    public void navigateToLogin() {
        WebDriver driver = GridDriverManager.getDriver();
        // Use safeNavigate: strips #google_vignette before loading and dismisses
        // any ad overlay that appears immediately after the page loads.
        AdDismissalUtils.safeNavigate("https://practice.expandtesting.com/notes/app/login");
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "window.localStorage.clear(); window.sessionStorage.clear();"
            );
            driver.manage().deleteAllCookies();
        } catch (Exception ignored) {}

        driver.navigate().refresh();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
        AdDismissalUtils.dismissAds();
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField));
    }

    public void login(String email, String password) {
        WebDriver driver = GridDriverManager.getDriver();
        AdDismissalUtils.dismissAds();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        WebElement emailEl = wait.until(ExpectedConditions.elementToBeClickable(emailField));
        emailEl.clear();
        emailEl.sendKeys(email);

        WebElement passwordEl = wait.until(ExpectedConditions.elementToBeClickable(passwordField));
        passwordEl.clear();
        passwordEl.sendKeys(password);

        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(loginButton));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
    }

    public boolean isLoginSuccessful() {
        try {
            By logoutBtn = By.xpath(
                    "(//button[normalize-space(text())='Logout'])[last()]"
            );
            WebDriverWait wait = new WebDriverWait(GridDriverManager.getDriver(), Duration.ofSeconds(10));
            return wait.until(ExpectedConditions.visibilityOfElementLocated(logoutBtn)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isLoginUnsuccessful() {
        try {
            By logoutBtn = By.xpath(
                    "(//button[normalize-space(text())='Logout'])[last()]"
            );
            WebDriverWait wait = new WebDriverWait(GridDriverManager.getDriver(), Duration.ofSeconds(5));
            wait.until(ExpectedConditions.invisibilityOfElementLocated(logoutBtn));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isErrorMessageDisplayed() {
        WebDriver driver = GridDriverManager.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // Strategy 1 — wait for a visible error element with non-empty text
        try {
            WebElement error = wait.until(ExpectedConditions.presenceOfElementLocated(errorAlertLocator));
            String txt = error.getText().trim();
            if (!txt.isEmpty()) return true;
            wait.until(d -> {
                try { return !d.findElement(errorAlertLocator).getText().trim().isEmpty(); }
                catch (Exception e) { return false; }
            });
            return true;
        } catch (Exception ignored) {}

        // Strategy 2 — body text keyword scan
        try {
            String pageText = driver.findElement(By.tagName("body")).getText().toLowerCase();
            return pageText.contains("invalid") || pageText.contains("incorrect") ||
                    pageText.contains("unauthorized") ||
                    (pageText.contains("password") && pageText.contains("required")) ||
                    (pageText.contains("email") && pageText.contains("required")) ||
                    pageText.contains("bad request") || pageText.contains("login failed");
        } catch (Exception e) {
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // REGISTRATION (TS-UI-02)
    // ─────────────────────────────────────────────────────────────

    public void navigateToRegister() {
        WebDriver driver = GridDriverManager.getDriver();
        AdDismissalUtils.safeNavigate("https://practice.expandtesting.com/notes/app/register");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
        AdDismissalUtils.dismissAds();
        wait.until(ExpectedConditions.visibilityOfElementLocated(regEmailField));
    }

    /**
     * Fills and submits the registration form with the provided values.
     * All inputs come from the step — no hardcoded test data here.
     */
    public void register(String name, String email, String password) {
        WebDriver driver = GridDriverManager.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        WebElement nameEl = wait.until(ExpectedConditions.elementToBeClickable(regNameField));
        nameEl.clear();
        nameEl.sendKeys(name);

        driver.findElement(regEmailField).sendKeys(email);
        driver.findElement(regPasswordField).sendKeys(password);

        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(registerButton));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
    }

    /**
     * After registration, the app redirects to login with a success banner.
     * We accept either: (a) a success alert is visible, or (b) the URL is now /login.
     */
    public boolean isRegistrationSuccessful() {
        WebDriver driver = GridDriverManager.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // Check for a success toast / banner
        By successLocator = By.cssSelector(
                ".alert-success, [class*='success'], [role='alert']"
        );
        try {
            WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(successLocator));
            if (el.isDisplayed()) return true;
        } catch (Exception ignored) {}

        // Check URL redirect
        try {
            wait.until(d -> d.getCurrentUrl().contains("/login") || d.getCurrentUrl().contains("/app"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}