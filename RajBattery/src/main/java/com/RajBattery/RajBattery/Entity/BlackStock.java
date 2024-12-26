package com.RajBattery.RajBattery.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;

@Entity
public class BlackStock {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "blackStock_seq")
    @SequenceGenerator(name = "blackStock_seq")
    Long id;
    Long productSelect;
    Integer openingStk;
    Integer issueStk;
    Integer returnStk;
    Integer closingStk;
    Integer recStock;
    String user;
    String doc;
    String note;
    @CreationTimestamp
    private LocalDate arrivalDate;

    public BlackStock() {
    }

    public BlackStock(Long id, Long productSelect, Integer openingStk, Integer issueStk, Integer returnStk, Integer closingStk, Integer recStock, String user, String doc, String note, LocalDate arrivalDate) {
        this.id = id;
        this.productSelect = productSelect;
        this.openingStk = openingStk;
        this.issueStk = issueStk;
        this.returnStk = returnStk;
        this.closingStk = closingStk;
        this.recStock = recStock;
        this.user = user;
        this.doc = doc;
        this.note = note;
        this.arrivalDate = arrivalDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductSelect() {
        return productSelect;
    }

    public void setProductSelect(Long productSelect) {
        this.productSelect = productSelect;
    }

    public Integer getOpeningStk() {
        return openingStk;
    }

    public void setOpeningStk(Integer openingStk) {
        this.openingStk = openingStk;
    }

    public Integer getIssueStk() {
        return issueStk;
    }

    public void setIssueStk(Integer issueStk) {
        this.issueStk = issueStk;
    }

    public Integer getReturnStk() {
        return returnStk;
    }

    public void setReturnStk(Integer returnStk) {
        this.returnStk = returnStk;
    }

    public Integer getClosingStk() {
        return closingStk;
    }

    public void setClosingStk(Integer closingStk) {
        this.closingStk = closingStk;
    }

    public Integer getRecStock() {
        return recStock;
    }

    public void setRecStock(Integer recStock) {
        this.recStock = recStock;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getDoc() {
        return doc;
    }

    public void setDoc(String doc) {
        this.doc = doc;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDate getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(LocalDate arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    @Override
    public String toString() {
        return "BlackStock{" +
                "id=" + id +
                ", productSelect=" + productSelect +
                ", openingStk=" + openingStk +
                ", issueStk=" + issueStk +
                ", returnStk=" + returnStk +
                ", closingStk=" + closingStk +
                ", recStock=" + recStock +
                ", user='" + user + '\'' +
                ", doc='" + doc + '\'' +
                ", note='" + note + '\'' +
                ", arrivalDate=" + arrivalDate +
                '}';
    }
}
