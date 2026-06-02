package com.expandtesting.base;

import com.expandtesting.config.ConfigReader;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import io.qameta.allure.Allure;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import static org.hamcrest.Matchers.lessThan;


public class BaseApi {


    protected static final int    RETRY_MAX_ATTEMPTS  = 3;
    protected static final long   RETRY_BACKOFF_MS    = 500L;
    protected static final int[]  RETRY_ON_STATUS     = {500, 503, 429};

    protected static final RequestSpecification requestSpec;
    protected static final ResponseSpecification slaSpec;

    static {
        RestAssured.baseURI = ConfigReader.getProperty("api.base.url");

        requestSpec = new RequestSpecBuilder()
                .setBaseUri(ConfigReader.getProperty("api.base.url"))
                .setContentType(ContentType.JSON)
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();


        slaSpec = new ResponseSpecBuilder()
                .expectResponseTime(lessThan(2000L))
                .build();
    }


    protected Response executeWithRetry(String operationName, Supplier<Response> apiCall) {
        Response lastResponse = null;
        Exception lastException = null;

        for (int attempt = 1; attempt <= RETRY_MAX_ATTEMPTS; attempt++) {
            try {
                lastException = null;
                lastResponse  = apiCall.get();

                int status = lastResponse.getStatusCode();
                if (!isTransientStatus(status)) {
                    if (attempt > 1) {
                        System.out.printf("[Retry] %s succeeded on attempt %d (status %d)%n",
                                operationName, attempt, status);
                    }
                    return lastResponse;
                }

                System.out.printf("[Retry] %s got transient status %d on attempt %d/%d%n",
                        operationName, status, attempt, RETRY_MAX_ATTEMPTS);

            } catch (Exception ex) {
                lastException = ex;
                System.out.printf("[Retry] %s threw %s on attempt %d/%d: %s%n",
                        operationName, ex.getClass().getSimpleName(),
                        attempt, RETRY_MAX_ATTEMPTS, ex.getMessage());
            }

            if (attempt < RETRY_MAX_ATTEMPTS) {
                long waitMs = RETRY_BACKOFF_MS * attempt;
                System.out.printf("[Retry] Waiting %d ms before attempt %d/%d …%n",
                        waitMs, attempt + 1, RETRY_MAX_ATTEMPTS);

                try { Thread.sleep(waitMs); } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (lastException != null) {
            throw new RuntimeException(
                    "[Retry] " + operationName + " failed after " + RETRY_MAX_ATTEMPTS
                    + " attempts: " + lastException.getMessage(), lastException);
        }
        return lastResponse;
    }


    protected boolean isTransientStatus(int status) {
        for (int s : RETRY_ON_STATUS) {
            if (s == status) return true;
        }
        return false;
    }

    protected void attachResponseToAllure(String attachmentName, Response response) {
        if (response == null) return;
        try {
            String body = response.getBody().asPrettyString();
            if (body == null || body.isBlank()) {
                body = "<empty response body>";
            }

            String fullContent = "HTTP " + response.getStatusCode()
                    + " " + response.getStatusLine() + "\n\n" + body;

            byte[] bytes = fullContent.getBytes(StandardCharsets.UTF_8);
            Allure.addAttachment(
                    attachmentName,
                    "application/json",
                    new ByteArrayInputStream(bytes),
                    ".json"
            );
        } catch (Exception e) {
            System.out.println("[BaseApi] Could not attach response to Allure: " + e.getMessage());
        }
    }
}
