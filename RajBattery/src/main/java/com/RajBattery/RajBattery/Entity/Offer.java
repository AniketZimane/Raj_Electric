package com.RajBattery.RajBattery.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.util.Arrays;

@Entity
public class Offer {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "offer_seq")
    @SequenceGenerator(name = "offer_seq")
    private Long id;
    private String offerName;
    private String description;
    private String endDate;

    @Lob
    @Column(name = "banner", columnDefinition = "LONGBLOB")
    private byte[] banner;


    @CreationTimestamp
    private LocalDate arrivalDate;

    @PrePersist
    private void prePersist() {
        arrivalDate = LocalDate.now();
    }

    public Offer() {
    }

    public Offer(Long id, String offerName, String description, byte[] banner, LocalDate arrivalDate) {
        this.id = id;
        this.offerName = offerName;
        this.description = description;
        this.banner = banner;
        this.arrivalDate = arrivalDate;
    }

    public Offer(Long id, String offerName, String description, String endDate, byte[] banner, LocalDate arrivalDate) {
        this.id = id;
        this.offerName = offerName;
        this.description = description;
        this.endDate = endDate;
        this.banner = banner;
        this.arrivalDate = arrivalDate;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOfferName() {
        return offerName;
    }

    public void setOfferName(String offerName) {
        this.offerName = offerName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getBanner() {
        return banner;
    }

    public void setBanner(byte[] banner) {
        this.banner = banner;
    }

    public LocalDate getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(LocalDate arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        return "Offer{" +
                "id=" + id +
                ", offerName='" + offerName + '\'' +
                ", description='" + description + '\'' +
                ", endDate='" + endDate + '\'' +
                ", banner=" + Arrays.toString(banner) +
                ", arrivalDate=" + arrivalDate +
                '}';
    }
}
