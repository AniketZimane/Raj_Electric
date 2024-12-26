package com.RajBattery.RajBattery.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Id;

@Entity
public class Registration {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "Reg_seq")
    @SequenceGenerator(name = "Reg_seq")
    Integer id;
    String username;
    String email;
    String mobNo;
    String password;
    String userType;

    public Registration() {
    }

    public Registration(Integer id, String userName, String email, String mobNo, String password) {
        this.id = id;
        this.username = userName;
        this.email = email;
        this.mobNo = mobNo;
        this.password = password;
    }

    public Registration(Integer id, String userName, String email, String mobNo, String password, String userType) {
        this.id = id;
        this.username = userName;
        this.email = email;
        this.mobNo = mobNo;
        this.password = password;
        this.userType = userType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobNo() {
        return mobNo;
    }

    public void setMobNo(String mobNo) {
        this.mobNo = mobNo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    @Override
    public String toString() {
        return "Registration{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", mobNo='" + mobNo + '\'' +
                ", password='" + password + '\'' +
                ", userType='" + userType + '\'' +
                '}';
    }
}
