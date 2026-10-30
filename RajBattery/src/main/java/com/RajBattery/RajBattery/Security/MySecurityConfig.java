package com.RajBattery.RajBattery.Security;

import com.RajBattery.RajBattery.Entity.Registration;
import com.RajBattery.RajBattery.Repo.RegistrationRepo;
import com.RajBattery.RajBattery.Service.CustomUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class MySecurityConfig {

    private final CustomUserDetailService myUserDetailService;

    @Autowired private RateLimitingFilter rateLimitingFilter;

    public MySecurityConfig(CustomUserDetailService myUserDetailService) {
        this.myUserDetailService = myUserDetailService;
    }

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
                .contentTypeOptions(ct -> {})
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true).maxAgeInSeconds(31536000))
                .referrerPolicy(ref -> ref
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/user_assets/**", "/assestad/**",
                    "/", "/login", "/signup", "/user/register/",
                    "/loader", "/user/products",
                    "/organization/setup", "/organization/save",
                    "/register-company/"
                ).permitAll()
                .requestMatchers("/it/**").hasRole("IT")
                .requestMatchers(
                    "/admin/**", "/Product/report/**", "/Product/stock/report/",
                    "/company/**", "/save-item/**", "/fetch-details/**",
                    "/create-invoice/**", "/offers/**",
                    "/vendor/**", "/purchase-order/**", "/purchase-orders/**",
                    "/new/Product/**", "/api/products/**", "/api/purchase-order/**",
                    "/api/vendors/**"
                ).hasRole("ADMIN")
                .requestMatchers("/user/**").hasRole("USER")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/default/", true)
                .failureUrl("/login?error=true")
            )
            .logout(logout -> logout
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/")
                .permitAll()
            )
            .rememberMe(remember -> remember
                .rememberMeParameter("remember_me")
                .key("erpSecretKey2024!")
                .tokenValiditySeconds(60 * 60 * 24 * 7)
            )
            .sessionManagement(session -> session
                .maximumSessions(3)
                .maxSessionsPreventsLogin(false)
            );

        return http.build();
    }

    @Bean
    public FilterRegistrationBean<RateLimitingFilter> rateLimitFilter() {
        FilterRegistrationBean<RateLimitingFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(rateLimitingFilter);
        reg.addUrlPatterns("/*");
        reg.setOrder(1);
        return reg;
    }

    @Bean
    public AuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(myUserDetailService);
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /** Seeds the IT user on first startup if not already present */
    @Bean
    public ApplicationRunner itUserSeeder(RegistrationRepo repo, PasswordEncoder encoder) {
        return (ApplicationArguments args) -> {
            String itEmail = "ituser@erp.com";
            if (repo.getByEmail(itEmail) == null) {
                Registration it = new Registration();
                it.setUsername("IT User");
                it.setEmail(itEmail);
                it.setPassword(encoder.encode("ERP@1604"));
                it.setUserType("2");
                it.setStatus("APPROVED");
                it.setMobNo("");
                repo.save(it);
                System.out.println("[ERP] IT user seeded: " + itEmail);
            }
        };
    }
}
