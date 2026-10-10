package com.example.demo.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import static org.assertj.core.api.Assertions.assertThat;

class LoggingConfigTest {

    @Test
    @DisplayName("Should create and configure CommonsRequestLoggingFilter bean")
    void shouldConfigureRequestLoggingFilter() {
        LoggingConfig loggingConfig = new LoggingConfig();
        CommonsRequestLoggingFilter filter = loggingConfig.requestLoggingFilter();

        assertThat(filter).isNotNull();
    }

    @Test
    @DisplayName("Should handle application ready event logging without errors")
    void shouldHandleApplicationReadyEvent() {
        LoggingConfig loggingConfig = new LoggingConfig();
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(loggingConfig::onApplicationReady);
    }
}
