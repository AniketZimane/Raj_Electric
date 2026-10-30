package com.RajBattery.RajBattery.Service;

import com.RajBattery.RajBattery.Entity.Registration;
import com.RajBattery.RajBattery.Repo.RegistrationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    @Autowired private RegistrationRepo registrationRepo;
    @Autowired private PasswordEncoder passwordEncoder;

    public boolean existsByEmail(String email) {
        return registrationRepo.getByEmail(email) != null;
    }

    /** Public signup — always PENDING, userType null until IT approves */
    public void register(Registration reg) {
        reg.setPassword(passwordEncoder.encode(reg.getPassword()));
        reg.setUserType("0");
        reg.setStatus("PENDING");
        registrationRepo.save(reg);
    }

    public Integer getOnlineCustomerCount() {
        Integer count = registrationRepo.findCountByUserType();
        return count != null ? count : 0;
    }

    // ── IT user operations ────────────────────────────────────────────────────

    public List<Registration> getPendingRegistrations() {
        return registrationRepo.findByStatusOrderByRegisteredAtDesc("PENDING");
    }

    public List<Registration> getAllRegistrations() {
        return registrationRepo.findAllByOrderByRegisteredAtDesc();
    }

    public List<Registration> getByCompany(Long companyId) {
        return registrationRepo.findByCompanyIdOrderByRegisteredAtDesc(companyId);
    }

    public void approve(Integer id, String approvedBy, String userType, Long companyId) {
        registrationRepo.findById(id).ifPresent(reg -> {
            reg.setStatus("APPROVED");
            reg.setApprovedBy(approvedBy);
            reg.setApprovedAt(LocalDateTime.now());
            reg.setUserType(userType);   // "0" = USER, "1" = ADMIN
            if (companyId != null) reg.setCompanyId(companyId);
            registrationRepo.save(reg);
        });
    }

    public void reject(Integer id, String approvedBy) {
        registrationRepo.findById(id).ifPresent(reg -> {
            reg.setStatus("REJECTED");
            reg.setApprovedBy(approvedBy);
            reg.setApprovedAt(LocalDateTime.now());
            registrationRepo.save(reg);
        });
    }

    public Registration getById(Integer id) {
        return registrationRepo.findById(id).orElse(null);
    }
}
