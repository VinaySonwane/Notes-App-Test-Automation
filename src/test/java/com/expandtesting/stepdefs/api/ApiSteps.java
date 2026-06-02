package com.expandtesting.stepdefs.api;

import com.expandtesting.api.NotesApiManager;
import com.expandtesting.config.ConfigReader;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.testng.Assert;


public class ApiSteps {

    NotesApiManager apiManager = new NotesApiManager();

    // Shared state across steps in the same scenario
    private Response lastResponse;
    private String   createdNoteId;
    private String   createdNoteTitle;
    private String   createdNoteCategory;



    // ─── Auth ───
    @Given("the user is authenticated via the API")
    public void theUserIsAuthenticatedViaTheAPI() {
        apiManager.authenticate(
                ConfigReader.getProperty("test.email"),
                ConfigReader.getProperty("test.password")
        );
    }

    // ─── TS-API-01: Login token ──────
    @When("the user sends a POST login request with email {string} and password {string}")
    public void userSendsPostLoginRequest(String email, String password) {
        lastResponse = apiManager.loginAndGetResponse(email, password);
    }

    @Then("the response status should be {int}")
    public void responseStatusShouldBe(int expectedStatus) {
        Assert.assertEquals(lastResponse.getStatusCode(), expectedStatus,
                "Expected status " + expectedStatus + " but got: " + lastResponse.getStatusCode());
    }

    @Then("the response should contain a valid auth token")
    public void responseShouldContainToken() {
        String token = lastResponse.jsonPath().getString("data.token");
        Assert.assertNotNull(token, "Auth token is null in response");
        Assert.assertFalse(token.trim().isEmpty(), "Auth token is empty in response");
    }

    // ─── TS-API-02: GET notes SLA ──────

    @When("the user requests to fetch all notes with seed title {string} description {string} category {string}")
    public void theUserRequestsToFetchAllNotes(String seedTitle,String description,String category) {
        apiManager.createNoteViaApi(seedTitle, description , category);
    }

    @Then("the API response should contain the note {string}")
    public void theAPIResponseShouldContainNote(String title) {
        Assert.assertTrue(
                apiManager.verifyNoteExists(title),
                "Failed to retrieve note from the API: " + title
        );
    }

    // ─── TS-API-03: POST create note ─────

    @When("the user creates a note via API with title {string} description {string} and category {string}")
    public void userCreatesNoteViaApi(String title, String description, String category) {
        createdNoteTitle    = title;
        createdNoteCategory = category;
        lastResponse = apiManager.createNoteAndGetResponse(title, description, category);
    }

    @Then("the API should return status {int} and the note should exist")
    public void apiShouldReturnStatusAndNoteExists(int expectedStatus) {
        Assert.assertEquals(lastResponse.getStatusCode(), expectedStatus,
                "Note creation returned wrong status. Body: " + lastResponse.getBody().asString());
        Assert.assertTrue(
                apiManager.verifyNoteExists(createdNoteTitle),
                "Created note not found via GET /notes: " + createdNoteTitle
        );
    }

    // ─── Shared setup for PUT / GET-by-ID ───
    @And("a note is created via the API with title {string} description {string} category {string}")
    public void noteCreatedViaAPIWithDetails(String title, String description, String category) {
        createdNoteTitle    = title;
        createdNoteCategory = category;
        createdNoteId = apiManager.createAndReturnNoteId(title, description, category);
        Assert.assertNotNull(createdNoteId,
                "Could not create a note for update testing: " + title);
    }

    // ─── TS-API-04: PUT update ───

    @When("the user sends a PUT request to update with title {string} and description {string}")
    public void userSendsPutRequest(String updatedTitle, String updatedDesc) {
        lastResponse = apiManager.putNote(
                createdNoteId, updatedTitle, updatedDesc, createdNoteCategory
        );
        createdNoteTitle = updatedTitle;
    }

    @Then("the API should return {int} and reflect the updated values")
    public void apiShouldReturnAndReflectUpdatedValues(int expectedStatus) {
        Assert.assertEquals(lastResponse.getStatusCode(), expectedStatus,
                "PUT returned wrong status. Body: " + lastResponse.getBody().asString());
        String returnedTitle = lastResponse.jsonPath().getString("data.title");
        Assert.assertEquals(returnedTitle, createdNoteTitle,
                "PUT response title does not match the title we sent");
    }

    // ─── TS-API-05: GET by ID ───

    @When("the user sends a GET request for that specific note ID")
    public void userSendsGetByIdRequest() {
        lastResponse = apiManager.getNoteById(createdNoteId);
    }

    @Then("the API should return {int} and the note data should match")
    public void apiShouldReturnAndNoteDataShouldMatch(int expectedStatus) {
        Assert.assertEquals(lastResponse.getStatusCode(), expectedStatus,
                "GET by ID returned wrong status. Body: " + lastResponse.getBody().asString());
        String returnedId = lastResponse.jsonPath().getString("data.id");
        Assert.assertEquals(returnedId, createdNoteId,
                "Returned note ID does not match the one we created");
    }

    // ─── TS-API-06: DELETE note by valid ID ───

    @When("the user sends a DELETE request for that specific note ID")
    public void userSendsDeleteByIdRequest() {
        lastResponse = apiManager.deleteNoteById(createdNoteId);
    }

    @Then("the API should return {int} and the note should no longer exist")
    public void apiShouldReturnAndNoteNoLongerExists(int expectedStatus) {
        Assert.assertEquals(lastResponse.getStatusCode(), expectedStatus,
                "DELETE returned wrong status. Body: " + lastResponse.getBody().asString());

        // Verify the note is actually gone — GET /notes should no longer contain it
        Assert.assertFalse(
                apiManager.verifyNoteExists(createdNoteTitle),
                "Note still exists in GET /notes after deletion: " + createdNoteTitle
        );
    }

    // ─── TS-NEG-04: POST missing title ────

    @When("the user sends a POST notes request with missing title but description {string} and category {string}")
    public void userSendsPostNotesWithMissingTitle(String description, String category) {
        lastResponse = apiManager.createNoteWithMissingTitle(description, category);
    }

    @Then("the API should return a {int} Bad Request status")
    public void apiShouldReturnBadRequest(int expectedStatus) {
        Assert.assertEquals(lastResponse.getStatusCode(), expectedStatus,
                "Expected " + expectedStatus + " for missing title but got: "
                        + lastResponse.getStatusCode()
                        + ". Body: " + lastResponse.getBody().asString());
    }

    // ─── TS-NEG-03: API Security — unauthorized access ────

    @When("a GET request is sent to {string} without an auth token")
    public void getRequestWithoutToken(String endpoint) {
        lastResponse = apiManager.getNotesWithoutToken();
    }

    @Then("the API should return a {int} Unauthorized status")
    public void apiShouldReturnUnauthorized(int expectedStatus) {
        Assert.assertEquals(lastResponse.getStatusCode(), expectedStatus,
                "API did not block unauthorized access!");
    }

    // ─── TS-NEG-05: Delete invalid note ID ────

    @When("a DELETE request is sent for an invalid note ID {string}")
    public void deleteRequestInvalidId(String invalidId) {
        apiManager.authenticate(
                ConfigReader.getProperty("test.email"),
                ConfigReader.getProperty("test.password")
        );
        lastResponse = apiManager.deleteInvalidNote(invalidId);
    }

    @Then("the API should return a {int} or {int} error status code")
    public void apiShouldReturnErrorStatus(int status1, int status2) {
        int actualStatus = lastResponse.getStatusCode();
        Assert.assertTrue(actualStatus == status1 || actualStatus == status2,
                "Expected " + status1 + " or " + status2 + ", but got: " + actualStatus);
    }
}

