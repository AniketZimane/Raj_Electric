package com.RajBattery.RajBattery.Security;

import com.RajBattery.RajBattery.Service.CustomUserDetailService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class MySecurityConfig {

    private final CustomUserDetailService myUserDetailService;

    // ✅ Constructor injection (preferred over field injection)
    public MySecurityConfig(CustomUserDetailService myUserDetailService) {
        this.myUserDetailService = myUserDetailService;
    }

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // ⚠️ CSRF disabled — enable it for form-based apps in production
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/user_assets/**",
                                "/assestad/**",
                                "/",
                                "/login",
                                "/signup",
                                "/user/register/",
                                "/loader",
                                "/user/products"
                        ).permitAll()
                        .requestMatchers(
                                "/admin/**",
                                "/admin/dashboard/",
                                "/admin/update/paid/amount/"
                        ).hasRole("ADMIN")
                        .requestMatchers("/user/**").hasRole("USER")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        // .loginProcessingUrl("/user/login") // Uncomment if custom login processing URL
                        .defaultSuccessUrl("/default/", true)
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
                        .key("mySecreteKey")
                        .tokenValiditySeconds(60 * 60 * 24 * 7) // 7 days
                );

        return http.build();
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
        return new BCryptPasswordEncoder();
    }
}
