package com.RajBattery.RajBattery.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
public class Registration {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Reg_seq")
    @SequenceGenerator(name = "Reg_seq")
    Integer id;
    String username;
    String email;
    String mobNo;
    String password;

    /**
     * userType:
     *   "0" = ROLE_USER  (regular customer)
     *   "1" = ROLE_ADMIN (business admin)
     *   "2" = ROLE_IT    (IT/platform user — manages registrations)
     */
    String userType;

    /**
     * status:
     *   "PENDING"  — submitted, awaiting IT approval
     *   "APPROVED" — IT approved, can log in
     *   "REJECTED" — IT rejected
     */
    @Column(nullable = false)
    String status = "PENDING";

    /** FK to CompanyDetails.id — which organisation this user belongs to */
    Long companyId;

    /** Who approved / rejected this registration */
    String approvedBy;

    @CreationTimestamp
    @Column(updatable = false)
    LocalDateTime registeredAt;

    LocalDateTime approvedAt;

    public Registration() {}

    public Registration(Integer id, String userName, String email, String mobNo, String password) {
        this.id = id; this.username = userName; this.email = email;
        this.mobNo = mobNo; this.password = password;
    }

    public Registration(Integer id, String userName, String email, String mobNo, String password, String userType) {
        this.id = id; this.username = userName; this.email = email;
        this.mobNo = mobNo; this.password = password; this.userType = userType;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMobNo() { return mobNo; }
    public void setMobNo(String mobNo) { this.mobNo = mobNo; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    @Override
    public String toString() {
        return "Registration{id=" + id + ", username='" + username + "', email='" + email +
               "', userType='" + userType + "', status='" + status + "', companyId=" + companyId + "}";
    }
}
