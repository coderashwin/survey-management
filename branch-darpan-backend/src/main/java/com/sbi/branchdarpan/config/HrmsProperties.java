package com.sbi.branchdarpan.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hrms")
public record HrmsProperties(String baseUrl, String apiKey) {
}
