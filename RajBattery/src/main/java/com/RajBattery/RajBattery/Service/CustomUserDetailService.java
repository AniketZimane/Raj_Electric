package com.RajBattery.RajBattery.Service;

import com.RajBattery.RajBattery.Entity.Registration;
import com.RajBattery.RajBattery.Repo.RegistrationRepo;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class CustomUserDetailService implements UserDetailsService {

    private final RegistrationRepo registrationRepo;
    private final HttpSession session;

    public CustomUserDetailService(RegistrationRepo registrationRepo, HttpSession session) {
        this.registrationRepo = registrationRepo;
        this.session = session;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Registration user = registrationRepo.getByEmail(email);

        if (user == null) {
            throw new UsernameNotFoundException("Invalid credentials.");
        }

        // Block PENDING and REJECTED users from logging in
        if ("PENDING".equals(user.getStatus())) {
            throw new UsernameNotFoundException("Your registration is pending IT approval.");
        }
        if ("REJECTED".equals(user.getStatus())) {
            throw new UsernameNotFoundException("Your registration has been rejected. Contact IT support.");
        }

        session.setAttribute("key", user.getUsername());

        Set<GrantedAuthority> authorities = new HashSet<>();
        switch (user.getUserType() != null ? user.getUserType() : "") {
            case "1" -> authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            case "2" -> authorities.add(new SimpleGrantedAuthority("ROLE_IT"));
            default  -> authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return new User(user.getEmail(), user.getPassword(), authorities);
    }
}
