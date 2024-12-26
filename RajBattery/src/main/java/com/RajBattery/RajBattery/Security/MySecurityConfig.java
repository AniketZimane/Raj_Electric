package com.RajBattery.RajBattery.Security;

import com.RajBattery.RajBattery.Service.CustomUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class MySecurityConfig
{
    @Autowired
    CustomUserDetailService myUserDetailService;

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeHttpRequests()
                .requestMatchers("/user_assets/**","/assestad/**", "/home/","/login/","/logout/","/signup/","/user/register/","/loader/").permitAll()
                .requestMatchers("/admin/**","/admin/dashboard/").hasRole("ADMIN")
                .requestMatchers("/user/**","/logout/").hasRole("USER")

                .anyRequest()
                .authenticated()
                .and()
                .formLogin()
                .loginPage("/login/")
                .usernameParameter("email")
                .passwordParameter("password")
//                    .loginProcessingUrl("/user/login/")
                .defaultSuccessUrl("/default/", true)
                .and()
                .logout().invalidateHttpSession(true).clearAuthentication(true)
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout/"))
                .logoutSuccessUrl("/home/").permitAll()
                .and()
                .rememberMe().rememberMeParameter("remember_me").key("mySecreteKey").tokenValiditySeconds(60 * 15);

        return http.build();
    }

    @Bean
    public AuthenticationProvider daoAuthenticationProvider()
    {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(myUserDetailService);
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder()
    {
         return new BCryptPasswordEncoder();
//        return NoOpPasswordEncoder.getInstance();
    }
}