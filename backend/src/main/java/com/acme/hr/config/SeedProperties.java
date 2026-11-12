package com.acme.hr.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param enabled       seed on startup when the employee table is empty
 * @param employeeCount how many employees to generate
 * @param randomSeed    fixed seed so every environment gets the same data
 */
@ConfigurationProperties("app.seed")
public record SeedProperties(boolean enabled, int employeeCount, long randomSeed) {
}
