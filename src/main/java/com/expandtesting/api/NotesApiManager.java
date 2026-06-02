package com.expandtesting.api;

import com.expandtesting.base.BaseApi;
import com.expandtesting.utils.PerformanceLogger;
import io.restassured.http.ContentType;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.lessThan;

public class NotesApiManager extends BaseApi {

    protected String authToken;

    // AUTH
    public void authenticate(String email, String password) {
        String payload = buildJson("email", email, "password", password);

        long start = System.currentTimeMillis();
        Response response = executeWithRetry("authenticate POST /users/login", () ->
                given(requestSpec)
                        .body(payload)
                        .when().post("/users/login")
                        .then()
                        .statusCode(200)
                        .body(JsonSchemaValidator.matchesJsonSchemaInClasspath(
                                "schemas/login_response_schema.json"))
                        .spec(slaSpec)
                        .extract().response()
        );
        long elapsed = System.currentTimeMillis() - start;

        PerformanceLogger.logApiTiming("authenticate", "POST /users/login", elapsed, 2000);
        attachResponseToAllure("POST /users/login — authenticate", response);

        this.authToken = response.jsonPath().getString("data.token");
    }


    public Response loginAndGetResponse(String email, String password) {
        String payload = buildJson("email", email, "password", password);

        long start = System.currentTimeMillis();
        Response response = executeWithRetry("loginAndGetResponse POST /users/login", () ->
                given(requestSpec)
                        .body(payload)
                        .when().post("/users/login")
                        .then().extract().response()
        );
        long elapsed = System.currentTimeMillis() - start;

        if (response.getStatusCode() == 200) {
            response.then()
                    .body(JsonSchemaValidator.matchesJsonSchemaInClasspath(
                            "schemas/login_response_schema.json"));
        }

        PerformanceLogger.logApiTiming("TS-API-01", "POST /users/login", elapsed, 2000);
        // 2.5 — attach to Allure per scenario
        attachResponseToAllure("POST /users/login — TS-API-01 Response", response);
        return response;
    }


    // CREATE
    public void createNoteViaApi(String title, String description, String category) {
        String payload = buildNoteJson(title, description, category);

        long start = System.currentTimeMillis();
        Response response = executeWithRetry("createNoteViaApi POST /notes", () ->
                given(requestSpec)
                        .header("x-auth-token", authToken)
                        .body(payload)
                        .when().post("/notes")
                        .then()
                        .statusCode(200)
                        .spec(slaSpec)
                        .extract().response()
        );
        long elapsed = System.currentTimeMillis() - start;

        PerformanceLogger.logApiTiming("createNoteViaApi", "POST /notes", elapsed, 2000);
        attachResponseToAllure("POST /notes — createNoteViaApi Response", response);
    }

    public String createAndReturnNoteId(String title, String description, String category) {
        String payload = buildNoteJson(title, description, category);

        long start = System.currentTimeMillis();
        Response response = executeWithRetry("createAndReturnNoteId POST /notes", () ->
                given(requestSpec)
                        .header("x-auth-token", authToken)
                        .body(payload)
                        .when().post("/notes")
                        .then()
                        .statusCode(200)
                        .body(JsonSchemaValidator.matchesJsonSchemaInClasspath(
                                "schemas/create_note_response_schema.json"))
                        .spec(slaSpec)
                        .extract().response()
        );
        long elapsed = System.currentTimeMillis() - start;

        PerformanceLogger.logApiTiming("createAndReturnNoteId", "POST /notes", elapsed, 2000);
        attachResponseToAllure("POST /notes — createAndReturnNoteId Response", response);
        return response.path("data.id");
    }


    public Response createNoteAndGetResponse(String title, String description, String category) {
        String payload = buildNoteJson(title, description, category);

        long start = System.currentTimeMillis();
        Response response = executeWithRetry("createNoteAndGetResponse POST /notes", () ->
                given(requestSpec)
                        .header("x-auth-token", authToken)
                        .body(payload)
                        .when().post("/notes")
                        .then().extract().response()
        );
        long elapsed = System.currentTimeMillis() - start;

        if (response.getStatusCode() == 200) {
            response.then()
                    .body(JsonSchemaValidator.matchesJsonSchemaInClasspath(
                            "schemas/create_note_response_schema.json"))
                    .time(lessThan(2000L));
        }

        PerformanceLogger.logApiTiming("TS-API-03", "POST /notes", elapsed, 2000);
        attachResponseToAllure("POST /notes — TS-API-03 Response", response);
        return response;
    }


    // READ
    public boolean verifyNoteExists(String expectedTitle) {
        long start = System.currentTimeMillis();
        Response response = executeWithRetry("verifyNoteExists GET /notes", () ->
                given(requestSpec)
                        .header("x-auth-token", authToken)
                        .when().get("/notes")
                        .then()
                        .statusCode(200)
                        .body(JsonSchemaValidator.matchesJsonSchemaInClasspath(
                                "schemas/get_notes_response_schema.json"))
                        .spec(slaSpec)
                        .extract().response()
        );
        long elapsed = System.currentTimeMillis() - start;

        PerformanceLogger.logApiTiming("verifyNoteExists", "GET /notes", elapsed, 2000);
        attachResponseToAllure("GET /notes — verifyNoteExists Response", response);

        List<String> titles = response.jsonPath().getList("data.title");
        return titles != null && titles.contains(expectedTitle);
    }


    public Response getNoteById(String noteId) {
        long start = System.currentTimeMillis();
        Response response = executeWithRetry("getNoteById GET /notes/" + noteId, () ->
                given(requestSpec)
                        .header("x-auth-token", authToken)
                        .when().get("/notes/" + noteId)
                        .then()
                        .spec(slaSpec)
                        .extract().response()
        );
        long elapsed = System.currentTimeMillis() - start;

        PerformanceLogger.logApiTiming("TS-API-05", "GET /notes/:id", elapsed, 2000);
        attachResponseToAllure("GET /notes/" + noteId + " — TS-API-05 Response", response);
        return response;
    }

    public Response getNotesWithoutToken() {
        Response response = executeWithRetry("getNotesWithoutToken GET /notes", () ->
                given(requestSpec)
                        .when().get("/notes")
                        .then().extract().response()
        );
        attachResponseToAllure("GET /notes — No-Auth Security Response", response);
        return response;
    }

    // UPDATE
    public Response putNote(String noteId, String title, String description, String category) {
        String payload = buildNoteJson(title, description, category);

        long start = System.currentTimeMillis();
        Response response = executeWithRetry("putNote PUT /notes/" + noteId, () ->
                given(requestSpec)
                        .header("x-auth-token", authToken)
                        .body(payload)
                        .when().put("/notes/" + noteId)
                        .then()
                        .spec(slaSpec)
                        .extract().response()
        );
        long elapsed = System.currentTimeMillis() - start;

        PerformanceLogger.logApiTiming("TS-API-04", "PUT /notes/:id", elapsed, 2000);
        attachResponseToAllure("PUT /notes/" + noteId + " — TS-API-04 Response", response);
        return response;
    }

    // DELETE
    public Response deleteNoteById(String noteId) {
        long start = System.currentTimeMillis();
        Response response = executeWithRetry("deleteNoteById DELETE /notes/" + noteId, () ->
                given(requestSpec)
                        .header("x-auth-token", authToken)
                        .when().delete("/notes/" + noteId)
                        .then()
                        .spec(slaSpec)
                        .extract().response()
        );
        long elapsed = System.currentTimeMillis() - start;

        PerformanceLogger.logApiTiming("TS-API-06", "DELETE /notes/:id", elapsed, 2000);
        attachResponseToAllure("DELETE /notes/" + noteId + " — TS-API-06 Response", response);
        return response;
    }

    public Response deleteInvalidNote(String invalidId) {
        Response response = executeWithRetry("deleteInvalidNote DELETE /notes/" + invalidId, () ->
                given(requestSpec)
                        .header("x-auth-token", authToken)
                        .when().delete("/notes/" + invalidId)
                        .then().extract().response()
        );
        attachResponseToAllure("DELETE /notes/" + invalidId + " — Invalid ID Response", response);
        return response;
    }


    // NEGATIVE
    public Response createNoteWithMissingTitle(String description, String category) {
        String payload = "{ \"description\": \"" + description + "\", \"category\": \"" + category + "\" }";

        long start = System.currentTimeMillis();
        Response response = executeWithRetry("createNoteWithMissingTitle POST /notes", () ->
                given(requestSpec)
                        .header("x-auth-token", authToken)
                        .body(payload)
                        .when().post("/notes")
                        .then().extract().response()
        );
        long elapsed = System.currentTimeMillis() - start;

        PerformanceLogger.logApiTiming("TS-NEG-04", "POST /notes (missing title)", elapsed, 2000);
        attachResponseToAllure("POST /notes — TS-NEG-04 Missing Title Response", response);
        return response;
    }

    public String getAuthToken() { return authToken; }



    // HELPERS
    private String buildNoteJson(String title, String description, String category) {
        return "{ \"title\": \"" + title
                + "\", \"description\": \"" + description
                + "\", \"category\": \"" + category
                + "\", \"completed\": false }";
    }

    private String buildJson(String k1, String v1, String k2, String v2) {
        return "{ \"" + k1 + "\": \"" + v1 + "\", \"" + k2 + "\": \"" + v2 + "\" }";
    }
}
