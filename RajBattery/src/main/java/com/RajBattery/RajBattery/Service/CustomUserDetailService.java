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

    // ✅ Constructor injection for both dependencies
    public CustomUserDetailService(RegistrationRepo registrationRepo, HttpSession session) {
        this.registrationRepo = registrationRepo;
        this.session = session;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("Login attempt with email: " + email);

        Registration user = registrationRepo.getByEmail(email);

        if (user == null) {
            throw new UsernameNotFoundException("Invalid User: " + email);
        }

        // ✅ Only set session after confirming user exists
        session.setAttribute("key", user.getUsername());
        System.out.println("Loaded user: " + user.getUsername());

        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        if ("1".equals(user.getUserType())) {
            System.out.println("Assigning ROLE_ADMIN");
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else if ("0".equals(user.getUserType())) {
            System.out.println("Assigning ROLE_USER");
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        } else {
            System.out.println("Unknown user type: " + user.getUserType());
        }

        return new User(user.getEmail(), user.getPassword(), grantedAuthorities);
    }
}
