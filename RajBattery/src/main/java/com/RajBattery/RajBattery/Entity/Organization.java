package com.RajBattery.RajBattery.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "organization")
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String orgName;

    @Column(nullable = false, unique = true)
    private String orgEmail;

    private String orgPhone;
    private String orgAddress;
    private String orgCity;
    private String orgState;
    private String orgPincode;
    private String gstNumber;
    private String panNumber;
    private String industry;       // e.g. Battery, Hardware, Electronics
    private String website;
    private boolean setupComplete = false;

    /**
     * status: "PENDING" | "APPROVED" | "REJECTED"
     * For self-registered companies awaiting IT approval.
     */
    @Column(nullable = false)
    private String status = "PENDING";

    private String approvedBy;

    private java.time.LocalDateTime approvedAt;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] logo;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public Organization() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrgName() { return orgName; }
    public void setOrgName(String orgName) { this.orgName = orgName; }
    public String getOrgEmail() { return orgEmail; }
    public void setOrgEmail(String orgEmail) { this.orgEmail = orgEmail; }
    public String getOrgPhone() { return orgPhone; }
    public void setOrgPhone(String orgPhone) { this.orgPhone = orgPhone; }
    public String getOrgAddress() { return orgAddress; }
    public void setOrgAddress(String orgAddress) { this.orgAddress = orgAddress; }
    public String getOrgCity() { return orgCity; }
    public void setOrgCity(String orgCity) { this.orgCity = orgCity; }
    public String getOrgState() { return orgState; }
    public void setOrgState(String orgState) { this.orgState = orgState; }
    public String getOrgPincode() { return orgPincode; }
    public void setOrgPincode(String orgPincode) { this.orgPincode = orgPincode; }
    public String getGstNumber() { return gstNumber; }
    public void setGstNumber(String gstNumber) { this.gstNumber = gstNumber; }
    public String getPanNumber() { return panNumber; }
    public void setPanNumber(String panNumber) { this.panNumber = panNumber; }
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public boolean isSetupComplete() { return setupComplete; }
    public void setSetupComplete(boolean setupComplete) { this.setupComplete = setupComplete; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public java.time.LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(java.time.LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public byte[] getLogo() { return logo; }
    public void setLogo(byte[] logo) { this.logo = logo; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
