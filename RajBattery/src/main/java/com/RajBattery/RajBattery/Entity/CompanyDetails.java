package com.RajBattery.RajBattery.Entity;

import jakarta.persistence.*;

import java.util.Arrays;

@Entity
public class CompanyDetails {

    @Id
            @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "company_seq")
            @SequenceGenerator(name = "company_seq")
    Long id;
    String cName;
    String cAddress;
    String cNumber;
    String gstnYn;
    String gstNumber;
    String userId;
    @Lob
    @Column(name = "logo", columnDefinition = "LONGBLOB")
    private byte[] logo;

    public CompanyDetails() {
    }

    public CompanyDetails(Long id, String cName, String cAddress, String cNumber, String gstnYn, String gstNumber, String userId, byte[] logo) {
        this.id = id;
        this.cName = cName;
        this.cAddress = cAddress;
        this.cNumber = cNumber;
        this.gstnYn = gstnYn;
        this.gstNumber = gstNumber;
        this.userId = userId;
        this.logo = logo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getcName() {
        return cName;
    }

    public void setcName(String cName) {
        this.cName = cName;
    }

    public String getcAddress() {
        return cAddress;
    }

    public void setcAddress(String cAddress) {
        this.cAddress = cAddress;
    }

    public String getcNumber() {
        return cNumber;
    }

    public void setcNumber(String cNumber) {
        this.cNumber = cNumber;
    }

    public String getGstnYn() {
        return gstnYn;
    }

    public void setGstnYn(String gstnYn) {
        this.gstnYn = gstnYn;
    }

    public String getGstNumber() {
        return gstNumber;
    }

    public void setGstNumber(String gstNumber) {
        this.gstNumber = gstNumber;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public byte[] getLogo() {
        return logo;
    }

    public void setLogo(byte[] logo) {
        this.logo = logo;
    }

    @Override
    public String toString() {
        return "CompanyDetails{" +
                "id=" + id +
                ", cName='" + cName + '\'' +
                ", cAddress='" + cAddress + '\'' +
                ", cNumber='" + cNumber + '\'' +
                ", gstnYn='" + gstnYn + '\'' +
                ", gstNumber='" + gstNumber + '\'' +
                ", userId='" + userId + '\'' +
                ", logo=" + Arrays.toString(logo) +
                '}';
    }
}
