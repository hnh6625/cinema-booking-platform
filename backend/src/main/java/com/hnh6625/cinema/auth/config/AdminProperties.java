package com.hnh6625.cinema.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.admin")
public record AdminProperties(
        String email,
        String phone,
        String password
) {
    @Override
    public String toString() {
        return "AdminProperties{" +
                "email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
