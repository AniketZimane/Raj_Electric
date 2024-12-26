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
    String userName;
    String email;
    String mobNo;
    String password;

    public Registration() {
    }

    public Registration(Integer id, String userName, String email, String mobNo, String password) {
        this.id = id;
        this.userName = userName;
        this.email = email;
        this.mobNo = mobNo;
        this.password = password;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    @Override
    public String toString() {
        return "Registration{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                ", mobNo='" + mobNo + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

}
