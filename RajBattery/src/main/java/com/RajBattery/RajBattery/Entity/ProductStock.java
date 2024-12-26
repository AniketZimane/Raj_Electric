package com.RajBattery.RajBattery.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;

@Entity
public class ProductStock
{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "productStock_seq")
    @SequenceGenerator(name = "productStock_seq")
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

    @PrePersist
    private void prePersist() {
        arrivalDate = LocalDate.now();

    }


    public ProductStock(Long id, Long productSelect, Integer openingStk, Integer issueStk, Integer returnStk, Integer closingStk, Integer recStock, String user, String doc, String note, LocalDate arrivalDate) {
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

    public ProductStock() {
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

    public Integer getClosingStk() {
        return closingStk;
    }

    public void setClosingStk(Integer closingStk) {
        this.closingStk = closingStk;
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

    public Integer getReturnStk() {
        return returnStk;
    }

    public void setReturnStk(Integer returnStk) {
        this.returnStk = returnStk;
    }

    public Integer getRecStock() {
        return recStock;
    }

    public void setRecStock(Integer recStock) {
        this.recStock = recStock;
    }

    public LocalDate getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(LocalDate arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    @Override
    public String toString() {
        return "ProductStock{" +
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
