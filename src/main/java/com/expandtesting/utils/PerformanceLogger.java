package com.expandtesting.utils;

import io.qameta.allure.Allure;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PerformanceLogger {

    private static final String LOG_PATH = "target/performance-log.csv";
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static {
        if (!Files.exists(Paths.get(LOG_PATH))) {
            try {
                Files.createDirectories(Paths.get("target"));
                try (PrintWriter pw = new PrintWriter(new FileWriter(LOG_PATH, false))) {
                    pw.println("timestamp,type,scenario,metric,value_ms,threshold_ms,pass");
                }
            } catch (IOException ignored) {}
        }

    }

    public static void logUiTiming(String scenarioName, String metric,
                                   long valueMs, long thresholdMs) {
        log("UI", scenarioName, metric, valueMs, thresholdMs);
    }

    public static void logApiTiming(String scenarioName, String endpoint,
                                    long valueMs, long thresholdMs) {
        log("API", scenarioName, endpoint, valueMs, thresholdMs);
    }

    private static void log(String type, String scenario, String metric,
                             long valueMs, long thresholdMs) {

        boolean pass  = valueMs >= 0 && valueMs <= thresholdMs;
        String  ts    = LocalDateTime.now().format(FMT);
        String  line  = String.join(",", ts, type, escape(scenario),
                escape(metric), String.valueOf(valueMs),
                String.valueOf(thresholdMs), String.valueOf(pass));

        try (PrintWriter pw = new PrintWriter(new FileWriter(LOG_PATH, true))) {
            pw.println(line);
        } catch (IOException ignored) {}

        String allureText = String.format(
                "[PERF] %s | %s | %s | %d ms | threshold %d ms | %s",
                type, scenario, metric, valueMs, thresholdMs,
                pass ? "PASS" : "SLOW");
        try {
            Allure.addAttachment("Performance: " + metric,
                    "text/plain", allureText, ".txt");
        } catch (Exception ignored) {

        }
        System.out.println(allureText);
    }

    private static String escape(String s) {
        if (s == null) return "";
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}
