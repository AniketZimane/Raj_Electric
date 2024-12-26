package com.RajBattery.RajBattery.Service;

import com.RajBattery.RajBattery.Entity.Registration;
import com.RajBattery.RajBattery.Repo.RegistrationRepo;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.HashSet;
import java.util.Set;
@Service
public class CustomUserDetailService implements UserDetailsService {

    private RegistrationRepo registrationRepo;
    @Autowired
    HttpSession session;

    @Autowired
    public CustomUserDetailService(RegistrationRepo registrationRepo) {
        this.registrationRepo = registrationRepo;

    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println(email);
        Registration user = registrationRepo.getByEmail(email);
        session.setAttribute("key",user.getUsername());
        System.out.println(user.getPassword());
        if (user == null) {
            throw new UsernameNotFoundException("Invalid User");
        } else {
            Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
            if (user.getUserType().equals("1")) {
                System.out.println("admin");
                grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            } else if (user.getUserType().equals("0")) {
                System.out.println("user");
                grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            }
            else
            {
                System.out.println("No user found");
            }
            return new User(user.getEmail(), user.getPassword(), grantedAuthorities);
        }
    }
}