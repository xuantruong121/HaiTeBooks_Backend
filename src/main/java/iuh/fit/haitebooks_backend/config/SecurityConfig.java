package iuh.fit.haitebooks_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final AppUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    public SecurityConfig(AppUserDetailsService userDetailsService, JwtUtil jwtUtil) {
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil, userDetailsService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        /* ================= PUBLIC APIS ================= */
                        .requestMatchers("/api/auth/**").permitAll()

                        // Public GET
                        .requestMatchers(HttpMethod.GET, "/api/books/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reviews/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/ai/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/books/barcode/**").permitAll()

                        // 🔥 Public validate promotion
                        .requestMatchers(HttpMethod.GET, "/api/promotions/validate/**").permitAll()

                        // 🔥 VNPay endpoints - PUBLIC (VNPay server needs to call IPN)
                        .requestMatchers("/api/v1/payment/vnpay/ipn").permitAll()
                        .requestMatchers("/api/v1/payment/vnpay/return").permitAll()
                        // VNPay create payment - authenticated
                        .requestMatchers("/api/v1/payment/create").authenticated()

                        // Statistics - ADMIN only
                        .requestMatchers("/api/statistics/**").hasRole("ADMIN")

                        // WebSocket
                        .requestMatchers("/ws/**").permitAll()

                        // Public Files Proxy
                        .requestMatchers("/api/files/**").permitAll()

                        /* ================= SWAGGER ================= */
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/error"
                        ).permitAll()

                        /* ================= AUTHENTICATED USERS ================= */
                        .requestMatchers("/api/users/me", "/api/users/me/**").authenticated()
                        .requestMatchers("/api/orders/**").authenticated()
                        .requestMatchers("/api/payments/**").authenticated()
                        .requestMatchers("/api/cart/**").authenticated()
                        .requestMatchers("/api/favorites/**").authenticated()

                        // Reviews (POST/PUT/DELETE require login)
                        .requestMatchers(HttpMethod.POST, "/api/reviews/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/reviews/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/reviews/**").authenticated()

                        // Notifications
                        .requestMatchers("/api/notifications/**").authenticated()

                        /* ================= PROMOTIONS ================= */

                        // ✔ USER chỉ được validate và xem
                        .requestMatchers(HttpMethod.GET, "/api/promotions/**").permitAll()

                        // ✔ ADMIN CRUD + workflow
                        .requestMatchers(HttpMethod.POST, "/api/promotions/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/promotions/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/promotions/**").hasRole("ADMIN")

                        // Approve / Reject / Deactivate (ADMIN only)
                        .requestMatchers("/api/promotions/approve/**").hasRole("ADMIN")
                        .requestMatchers("/api/promotions/reject/**").hasRole("ADMIN")
                        .requestMatchers("/api/promotions/deactivate/**").hasRole("ADMIN")

                        /* ================= ADMIN ROUTES ================= */
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Books CRUD
                        .requestMatchers(HttpMethod.POST, "/api/books/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/books/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasRole("ADMIN")

                        // Categories CRUD
                        .requestMatchers(HttpMethod.POST, "/api/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasRole("ADMIN")

                        // Users
                        .requestMatchers("/api/users/all").hasRole("ADMIN")
                        .requestMatchers("/api/users/{id}").hasRole("ADMIN")

                        // AI - POST requires ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/ai/**").hasRole("ADMIN")

                        /* ================= DEFAULT ================= */
                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}