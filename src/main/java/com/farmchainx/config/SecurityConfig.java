package com.farmchainx.config;

import com.farmchainx.security.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Central Spring Security configuration.
 *
 * Strategy:
 * - Session-based authentication for Thymeleaf pages.
 * - REST endpoints (/api/**) share the same session; they return JSON 401/403 instead of redirects.
 * - CSRF is enabled globally (Thymeleaf forms include the token automatically).
 * - Public endpoints (login, register, consumer tracking, static assets) are permit-all.
 * - Role-based URL restrictions enforce the authorisation matrix.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // ── Password encoder ─────────────────────────────────────────────────────

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    // ── Authentication provider ──────────────────────────────────────────────

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // ── Authentication manager ───────────────────────────────────────────────

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ── Security filter chain ────────────────────────────────────────────────

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .authenticationProvider(authenticationProvider())

            // ── Authorization rules ──────────────────────────────────────────
            .authorizeHttpRequests(auth -> auth

                // Public pages and static resources
                .requestMatchers(
                    "/",
                    "/login",
                    "/register",
                    "/track",
                    "/consumer/track",
                    "/api/public/**",
                    "/api/auth/register",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/webjars/**",
                    "/actuator/health/liveness",
                    "/actuator/health/readiness",
                    "/error"
                ).permitAll()

                // Admin-only areas
                .requestMatchers("/admin/**", "/api/admin/**")
                    .hasRole("ADMIN")

                // Farmer areas
                .requestMatchers("/farmer/**")
                    .hasAnyRole("FARMER", "ADMIN")

                // Distributor areas
                .requestMatchers("/distributor/transportation/**")
                    .hasAnyRole("DISTRIBUTOR", "ADMIN")
                .requestMatchers("/distributor/**")
                    .hasRole("DISTRIBUTOR")

                // Warehouse manager areas
                .requestMatchers("/warehouse/manage/**")
                    .hasAnyRole("WAREHOUSE_MANAGER", "ADMIN")
                .requestMatchers("/warehouse/**")
                    .hasRole("WAREHOUSE_MANAGER")

                // Retailer areas
                .requestMatchers("/retailer/**")
                    .hasRole("RETAILER")

                // REST API – require authentication; fine-grained control via @PreAuthorize
                .requestMatchers("/api/**").authenticated()

                // All other requests require login
                .anyRequest().authenticated()
            )

            // ── Form login ───────────────────────────────────────────────────
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler((request, response, authentication) -> {
                    // Redirect based on role after successful login
                    String role = authentication.getAuthorities().iterator().next().getAuthority();
                    String redirect = switch (role) {
                        case "ROLE_ADMIN"             -> "/admin/dashboard";
                        case "ROLE_FARMER"            -> "/farmer/dashboard";
                        case "ROLE_DISTRIBUTOR"       -> "/distributor/dashboard";
                        case "ROLE_WAREHOUSE_MANAGER" -> "/warehouse/dashboard";
                        case "ROLE_RETAILER"          -> "/retailer/dashboard";
                        case "ROLE_CONSUMER"          -> "/consumer/track";
                        default                       -> "/dashboard";
                    };
                    response.sendRedirect(request.getContextPath() + redirect);
                })
                .failureUrl("/login?error=true")
                .permitAll()
            )

            // ── Logout ────────────────────────────────────────────────────────
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )

            // ── REST API: return 401/403 JSON instead of login redirects ──────
            .exceptionHandling(ex -> ex
                .defaultAuthenticationEntryPointFor(
                    (request, response, authException) -> {
                        response.setContentType("application/json;charset=UTF-8");
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write(
                            "{\"success\":false,\"message\":\"Authentication required\"}");
                    },
                    new AntPathRequestMatcher("/api/**")
                )
                .defaultAccessDeniedHandlerFor(
                    (request, response, accessDeniedException) -> {
                        // Determine if this is an API call or a browser navigation
                        String accept = request.getHeader("Accept");
                        if (accept != null && accept.contains("application/json")) {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.getWriter().write(
                                "{\"success\":false,\"message\":\"Access denied\"}");
                        } else {
                            response.sendRedirect(request.getContextPath() + "/access-denied");
                        }
                    },
                    new AntPathRequestMatcher("/**")
                )
            )

            // ── Session management ────────────────────────────────────────────
            .sessionManagement(session -> session
                .maximumSessions(1)
                .expiredUrl("/login?expired=true")
            );

        return http.build();
    }
}
