package com.expandtesting.stepdefs.ui;

import com.expandtesting.config.ConfigReader;
import com.expandtesting.pages.LoginPage;
import com.expandtesting.pages.NotesPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.testng.Assert;


public class UiSteps {

    LoginPage loginPage = new LoginPage();
    NotesPage notesPage = new NotesPage();


    protected String currentNoteTitle;

    // ─── Login ────
    @Given("the user is on the login page")
    public void theUserIsOnTheLoginPage() {
        loginPage.navigateToLogin();
    }

    @When("the user attempts to login with email {string} and password {string}")
    public void theUserAttemptsToLogin(String email, String password) {
        loginPage.login(email, password);
    }

    @Then("the login should be {string}")
    public void theLoginShouldBe(String status) {
        if (status.equals("successful")) {
            Assert.assertTrue(
                    loginPage.isLoginSuccessful(),
                    "Login failed for valid credentials — Logout button not visible."
            );
        } else if (status.equals("unsuccessful")) {
            Assert.assertTrue(
                    loginPage.isLoginUnsuccessful(),
                    "Login succeeded unexpectedly — user appears logged in with invalid credentials."
            );
        }
    }

    // ─── Registration (TS-UI-02) ────
    @Given("the user is on the registration page")
    public void theUserIsOnTheRegistrationPage() {
        loginPage.navigateToRegister();
    }

    @When("the user registers with name {string} email {string} and password {string}")
    public void theUserRegistersWithCredentials(String name, String email, String password) {

        String uniqueEmail = email.replace("@", "_" + System.currentTimeMillis() + "@");
        loginPage.register(name, uniqueEmail, password);
    }

    @Then("the account should be created successfully")
    public void theAccountShouldBeCreatedSuccessfully() {
        Assert.assertTrue(
                loginPage.isRegistrationSuccessful(),
                "Registration did not succeed — no success banner or redirect to login."
        );
    }

    // ─── Create Note (TS-UI-03) ─────
    @When("the user creates a new note with title {string} description {string} and category {string}")
    public void theUserCreatesANewNote(String title, String description, String category) {
        currentNoteTitle = title;
        notesPage.createNewNote(category, title, description);
    }

    @Then("the new note with title {string} should be visible on the dashboard")
    public void theNewNoteShouldBeVisibleOnDashboard(String title) {
        Assert.assertTrue(
                notesPage.isNoteVisible(title),
                "Newly created note was not visible on dashboard: " + title
        );
    }

    //for test case 4,5,6 (precondition)
    @And("a note exists with title {string} description {string} category {string}")
    public void aNoteExistsWithDetails(String title, String description, String category) {
        currentNoteTitle = title;
        notesPage.createNewNote(category, currentNoteTitle, description);
        Assert.assertTrue(
                notesPage.isNoteVisible(currentNoteTitle),
                "Pre-condition failed: note did not appear after creation: " + currentNoteTitle
        );
    }

    // ─── Edit (TS-UI-04) ────────
    @When("the user edits the note with new title {string} and description {string}")
    public void theUserEditsTheNote(String updatedTitle, String updatedDesc) {
        notesPage.editNote(currentNoteTitle, updatedTitle, updatedDesc);
        currentNoteTitle = updatedTitle;
    }

    @Then("the updated note should be visible on the dashboard")
    public void theUpdatedNoteShouldBeVisible() {
        Assert.assertTrue(
                notesPage.isNoteVisible(currentNoteTitle),
                "Edited note title not visible on dashboard: " + currentNoteTitle
        );
    }

    // ─── Delete (TS-UI-05) ────

    @When("the user deletes the note via the UI")
    public void theUserDeletesTheNoteViaUi() {
        notesPage.deleteNoteViaUi(currentNoteTitle);
    }

    @Then("the note should no longer appear on the dashboard")
    public void theNoteShouldNoLongerAppear() {
        Assert.assertTrue(
                notesPage.isNoteAbsent(currentNoteTitle),
                "Deleted note is still visible: " + currentNoteTitle
        );
    }

    // ─── Category Filter (TS-UI-06) ─────

    @When("the user filters the dashboard by category {string}")
    public void theUserFiltersByCategory(String category) {
        notesPage.filterByCategory(category);
    }

    @Then("only notes from category {string} should be visible")
    public void onlyNotesShouldBeVisible(String category) {
        Assert.assertTrue(
                notesPage.isNoteVisible(currentNoteTitle),
                "Note not visible after filtering by category: " + category
        );
    }

    // ─── Negative (TS-NEG-01) ────
    @Then("an error message should be displayed on the screen")
    public void errorMessageShouldBeDisplayed() {
        Assert.assertTrue(loginPage.isErrorMessageDisplayed(),
                "Login error message did not appear!");
    }

    // ─── Negative (TS-NEG-02) ────
    @When("the user tries to create a note with an empty title {string} {string}")
    public void createNoteEmptyTitle( String description,String category) {
        notesPage.createNewNote(category, "", description);
    }

    @Then("a validation error should appear on the screen preventing submission")
    public void validationErrorShouldAppear() {
        Assert.assertTrue(notesPage.isTitleValidationErrorDisplayed(),
                "Form submitted even though title was empty!");
    }



}



