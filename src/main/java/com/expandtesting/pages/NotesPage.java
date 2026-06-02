package com.expandtesting.pages;

import com.expandtesting.base.BaseUi;
import com.expandtesting.drivers.GridDriverManager;
import com.expandtesting.utils.AdDismissalUtils;
import com.expandtesting.utils.PerformanceLogger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;

public class NotesPage extends BaseUi {


    protected final By addNoteButton    = By.xpath("//button[@data-testid='add-new-note' or contains(text(),'Add')]");
    protected final By categoryDropdown = By.id("category");
    protected final By titleInput       = By.id("title");
    protected final By descriptionInput = By.id("description");
    protected final By createButton     = By.xpath("//button[@data-testid='note-submit']");

    protected final By editTitleInput       = By.id("title");
    protected final By editDescriptionInput = By.id("description");
    protected final By saveEditButton       = By.xpath("//button[@data-testid='note-submit']");


    // ─── Create
    public void createNewNote(String category, String title, String description) {
        WebDriver driver = GridDriverManager.getDriver();
        AdDismissalUtils.dismissAds();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(addNoteButton));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", addBtn);

        Select select = new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(categoryDropdown)));
        select.selectByVisibleText(category);

        WebElement titleEl = wait.until(ExpectedConditions.visibilityOfElementLocated(titleInput));
        titleEl.clear();
        titleEl.sendKeys(title);

        WebElement descEl = driver.findElement(descriptionInput);
        descEl.clear();
        descEl.sendKeys(description);

        WebElement submitBtn = wait.until(ExpectedConditions.presenceOfElementLocated(createButton));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitBtn);

    }

    //  Read
    public boolean isNoteVisible(String expectedTitle) {
        WebDriver driver = GridDriverManager.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        By locator = By.xpath("//*[contains(text(),'" + expectedTitle + "')]");
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    public boolean isNoteAbsent(String title) {
        WebDriver driver = GridDriverManager.getDriver();
        By locator = By.xpath("//*[contains(text(),'" + title + "')]");
        driver.navigate().refresh();

        new WebDriverWait(driver, Duration.ofSeconds(15))
                .until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
        AdDismissalUtils.dismissAds();

        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.invisibilityOfElementLocated(locator));
            return true;
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    // ─── Edit (TS-UI-04, TS-E2E-02)
    public void editNote(String existingTitle, String newTitle, String newDescription) {
        WebDriver driver = GridDriverManager.getDriver();
        AdDismissalUtils.dismissAds();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        By editBtn = By.xpath(
                "//*[contains(text(),'" + existingTitle + "')]"
                        + "/ancestor::div[contains(@class,'card')]"
                        + "//button[@data-testid='note-edit']"
        );

        WebElement edit = wait.until(ExpectedConditions.elementToBeClickable(editBtn));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", edit);

        WebElement titleEl = wait.until(ExpectedConditions.visibilityOfElementLocated(editTitleInput));
        titleEl.clear();
        titleEl.sendKeys(newTitle);

        WebElement descEl = driver.findElement(editDescriptionInput);
        descEl.clear();
        descEl.sendKeys(newDescription);

        WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(saveEditButton));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", saveBtn);
    }

    // ─── Delete via UI (TS-UI-05)
    public void deleteNoteViaUi(String noteTitle) {
        WebDriver driver = GridDriverManager.getDriver();
        AdDismissalUtils.dismissAds();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        By deleteBtn = By.xpath(
                "//*[contains(text(),'" + noteTitle + "')]"
                        + "/ancestor::div[contains(@class,'card')]"
                        + "//button[@data-testid='note-delete']"
        );
        WebElement del = wait.until(ExpectedConditions.elementToBeClickable(deleteBtn));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", del);

        try {
            By confirmBtn = By.xpath("//button[@data-testid='note-delete-confirm']");
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(confirmBtn))
                    .click();
        } catch (Exception ignored) {

        }
    }

    // ─── Category Filter (TS-UI-06)
    public void filterByCategory(String category) {
        WebDriver driver = GridDriverManager.getDriver();
        AdDismissalUtils.dismissAds();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        By categoryTab = By.xpath(
                "//button[@data-testid='category-" + category.toLowerCase() + "']"
        );
        WebElement tab = wait.until(ExpectedConditions.elementToBeClickable(categoryTab));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", tab);
    }

    // ─── Validation Error (TS-NEG-02) ─────────────────────────────
    public boolean isTitleValidationErrorDisplayed() {
        WebDriver driver = GridDriverManager.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        By titleInvalidInput = By.cssSelector("input#title.is-invalid");
        By titleErrorDiv     = By.cssSelector("input#title.is-invalid ~ div.invalid-feedback");
        try {
            // Step 1: input gets .is-invalid after form submit is processed
            wait.until(ExpectedConditions.presenceOfElementLocated(titleInvalidInput));
            // Step 2: sibling error div is now visible and has text
            WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(titleErrorDiv));
            return error.getText().trim().equalsIgnoreCase("Title is required");
        } catch (Exception ignored) {}

        By anyErrorDiv = By.cssSelector("div.invalid-feedback");
        try {
            wait.until(d -> {
                List<WebElement> els = d.findElements(anyErrorDiv);
                return els.stream().anyMatch(el -> {
                    String t = el.getText().trim().toLowerCase();
                    return t.contains("title") || t.contains("required");
                });
            });
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
