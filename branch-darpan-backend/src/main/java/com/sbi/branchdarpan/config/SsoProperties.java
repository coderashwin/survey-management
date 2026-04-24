package com.sbi.branchdarpan.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sso")
public record SsoProperties(String mode, String loginUrl, String callbackUrl) {
}
