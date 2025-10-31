package com.afriasdev.dds.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class MiscConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
