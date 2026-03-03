package com.build.CodeVault.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Binds the {@code storage.location} property from application.yml
 * to a type-safe configuration bean.
 */
@Configuration
@ConfigurationProperties(prefix = "storage")
@Getter
@Setter
public class StorageConfig {

    /**
     * Root directory for storing repository source code.
     * Example: D:/codevault-storage
     */
    private String location;
}
