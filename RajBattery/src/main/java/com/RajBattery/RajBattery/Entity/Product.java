package com.RajBattery.RajBattery.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;

@Entity
public class Product {
//    @Id

//    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq")
//    @SequenceGenerator(name = "product_seq")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long PId;
    String category;
    String PName;
    String CName;
    Double prize;
    String extension;
    String Pmode;
    String description;

    @CreationTimestamp
    private LocalDate arrivalDate;

    @PrePersist
    private void prePersist() {
        arrivalDate = LocalDate.now();

    }

    public Product() {
    }

    public Product(Long PId, String category, String PName, String CName, Double prize, String extension, String pmode, String description, LocalDate arrivalDate) {
        this.PId = PId;
        this.category = category;
        this.PName = PName;
        this.CName = CName;
        this.prize = prize;
        this.extension = extension;
        Pmode = pmode;
        this.description = description;
        this.arrivalDate = arrivalDate;
    }

    public Product(String category, String PName, String CName, Double prize, String extension, String pmode, String description, LocalDate arrivalDate) {
        this.category = category;
        this.PName = PName;
        this.CName = CName;
        this.prize = prize;
        this.extension = extension;
        Pmode = pmode;
        this.description = description;
        this.arrivalDate = arrivalDate;
    }

    public Long getPId() {
        return PId;
    }

    public void setPId(Long PId) {
        this.PId = PId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPName() {
        return PName;
    }

    public void setPName(String PName) {
        this.PName = PName;
    }

    public String getCName() {
        return CName;
    }

    public void setCName(String CName) {
        this.CName = CName;
    }


    public Double getPrize() {
        return prize;
    }

    public void setPrize(Double prize) {
        this.prize = prize;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getPmode() {
        return Pmode;
    }

    public void setPmode(String pmode) {
        Pmode = pmode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(LocalDate arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    @Override
    public String toString() {
        return "Product{" +
                "PId=" + PId +
                ", category='" + category + '\'' +
                ", PName='" + PName + '\'' +
                ", CName='" + CName + '\'' +
                ", prize=" + prize +
                ", extension='" + extension + '\'' +
                ", Pmode='" + Pmode + '\'' +
                ", description='" + description + '\'' +
                ", arrivalDate=" + arrivalDate +
                '}';
    }
}
