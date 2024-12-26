package com.RajBattery.RajBattery.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;

@Entity
public class InhouseInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inhouse_seq")
    @SequenceGenerator(name = "inhouse_seq")
    Long id;
    Integer categoryId;
    Long prodId;
    Long orderId;
    Integer prodQty;
    Double prodRate;
    Double prodTotalVal;
    String userName;
    String userAddress;
    String status;

    @CreationTimestamp
    private LocalDate invoiceDate;

    @PrePersist
    private void prePersist() {
        invoiceDate = LocalDate.now();

    }

    public InhouseInvoice() {
    }

    public InhouseInvoice(Long id, Integer categoryId, Long prodId, Long orderId, Integer prodQty, Double prodRate, Double prodTotalVal, String userName, String userAddress, String status, LocalDate invoiceDate) {
        this.id = id;
        this.categoryId = categoryId;
        this.prodId = prodId;
        this.orderId = orderId;
        this.prodQty = prodQty;
        this.prodRate = prodRate;
        this.prodTotalVal = prodTotalVal;
        this.userName = userName;
        this.userAddress = userAddress;
        this.status = status;
        this.invoiceDate = invoiceDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Long getProdId() {
        return prodId;
    }

    public void setProdId(Long prodId) {
        this.prodId = prodId;
    }

    public Integer getProdQty() {
        return prodQty;
    }

    public void setProdQty(Integer prodQty) {
        this.prodQty = prodQty;
    }

    public Double getProdRate() {
        return prodRate;
    }

    public void setProdRate(Double prodRate) {
        this.prodRate = prodRate;
    }

    public Double getProdTotalVal() {
        return prodTotalVal;
    }

    public void setProdTotalVal(Double prodTotalVal) {
        this.prodTotalVal = prodTotalVal;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        return "InhouseInvoice{" +
                "id=" + id +
                ", categoryId=" + categoryId +
                ", prodId=" + prodId +
                ", orderId=" + orderId +
                ", prodQty=" + prodQty +
                ", prodRate=" + prodRate +
                ", prodTotalVal=" + prodTotalVal +
                ", userName='" + userName + '\'' +
                ", userAddress='" + userAddress + '\'' +
                ", status='" + status + '\'' +
                ", invoiceDate=" + invoiceDate +
                '}';
    }
}
