package com.farmchainx.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Strongly-typed binding for the 'farmchainx' configuration namespace in application.yml.
 */
@Configuration
@ConfigurationProperties(prefix = "farmchainx")
public class AppProperties {

    private final Admin admin = new Admin();
    private final Upload upload = new Upload();
    private final Tracking tracking = new Tracking();

    public Admin getAdmin() {
        return admin;
    }

    public Upload getUpload() {
        return upload;
    }

    public Tracking getTracking() {
        return tracking;
    }

    public static class Tracking {
        private String prefix = "FCX";

        public String getPrefix() { return prefix; }
        public void setPrefix(String prefix) { this.prefix = prefix; }
    }

    // ── Nested: Admin ────────────────────────────────────────────────────────

    public static class Admin {
        private String email;
        private String password;
        private String fullName;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
    }

    // ── Nested: Upload ───────────────────────────────────────────────────────

    public static class Upload {
        private String dir = "./uploads";

        public String getDir() { return dir; }
        public void setDir(String dir) { this.dir = dir; }
    }
}
