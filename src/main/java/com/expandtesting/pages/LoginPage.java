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

    protected final By emailField    = By.id("email");
    protected final By passwordField = By.id("password");
    protected final By loginButton   = By.xpath("//button[@type='submit' or contains(text(),'Login')]");

    protected final By regNameField     = By.id("name");
    protected final By regEmailField    = By.id("email");
    protected final By regPasswordField = By.id("password");
    protected final By regConfirmPasswordField = By.id("confirmPassword");
    protected final By registerButton   = By.xpath("//button[@type='submit' or contains(text(),'Register')]");

    protected final By errorAlertLocator = By.cssSelector(
            "[role='alert'], [data-testid='alert-message'], div.alert-danger, " +
                    ".alert.alert-danger, .text-danger, .error-message, p.alert, " +
                    "div[class*='error'], div[class*='Error'], span[class*='error']"
    );

    // LOGIN
    public void navigateToLogin() {
        WebDriver driver = GridDriverManager.getDriver();

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
        WebDriverWait wait = new WebDriverWait(GridDriverManager.getDriver(), Duration.ofSeconds(15));
        try {
            WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(errorAlertLocator));
            return !error.getText().trim().isEmpty();
        } catch (Exception ignored) {
            return false;
        }
    }


    // REGISTRATION (TS-UI-02)
    public void navigateToRegister() {
        WebDriver driver = GridDriverManager.getDriver();
        AdDismissalUtils.safeNavigate("https://practice.expandtesting.com/notes/app/register");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
        AdDismissalUtils.dismissAds();
        wait.until(ExpectedConditions.visibilityOfElementLocated(regEmailField));
    }

    public void register(String name, String email, String password) {
        WebDriver driver = GridDriverManager.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        WebElement nameEl = wait.until(ExpectedConditions.elementToBeClickable(regNameField));
        nameEl.clear();
        nameEl.sendKeys(name);

        driver.findElement(regEmailField).sendKeys(email);
        driver.findElement(regPasswordField).sendKeys(password);
        driver.findElement(regConfirmPasswordField ).sendKeys(password);

        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(registerButton));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
    }

    public boolean isRegistrationSuccessful() {
        By successLocator = By.cssSelector("div.alert.alert-success");
        try {
            WebElement el = new WebDriverWait(GridDriverManager.getDriver(), Duration.ofSeconds(15))
                    .until(ExpectedConditions.visibilityOfElementLocated(successLocator));
            return el.getText().contains("User account created successfully");
        } catch (Exception ignored) {
            return false;
        }
    }

}