package com.RajBattery.RajBattery.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class OrderPlaced {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "orderPlaced_seq")
    @SequenceGenerator(name = "orderPlaced_seq")
    Long id;
    Double grossAmount;
    Double cgst;
    Double sgst;
    Double gst;
    Integer discount;
    Double netAmt;
    Double paidAmount;
    String paymentMode;
    String paymentStatus;
    String mobNumber;
    String username;
    String address;
    String emailId;

    @CreationTimestamp
    private LocalDateTime invoiceDate;

    @PrePersist
    private void prePersist() {
        invoiceDate = LocalDateTime.now();

    }

    public OrderPlaced() {
    }

    public OrderPlaced(Long id, Double grossAmount, Double cgst, Double sgst, Double gst, Integer discount, Double netAmt, Double paidAmount, String paymentMode, String paymentStatus, String mobNumber, String username, String address, String emailId, LocalDateTime invoiceDate) {
        this.id = id;
        this.grossAmount = grossAmount;
        this.cgst = cgst;
        this.sgst = sgst;
        this.gst = gst;
        this.discount = discount;
        this.netAmt = netAmt;
        this.paidAmount = paidAmount;
        this.paymentMode = paymentMode;
        this.paymentStatus = paymentStatus;
        this.mobNumber = mobNumber;
        this.username = username;
        this.address = address;
        this.emailId = emailId;
        this.invoiceDate = invoiceDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getGrossAmount() {
        return grossAmount;
    }

    public void setGrossAmount(Double grossAmount) {
        this.grossAmount = grossAmount;
    }

    public Double getCgst() {
        return cgst;
    }

    public void setCgst(Double cgst) {
        this.cgst = cgst;
    }

    public Double getSgst() {
        return sgst;
    }

    public void setSgst(Double sgst) {
        this.sgst = sgst;
    }

    public Double getGst() {
        return gst;
    }

    public void setGst(Double gst) {
        this.gst = gst;
    }

    public Integer getDiscount() {
        return discount;
    }

    public void setDiscount(Integer discount) {
        this.discount = discount;
    }

    public Double getNetAmt() {
        return netAmt;
    }

    public void setNetAmt(Double netAmt) {
        this.netAmt = netAmt;
    }

    public Double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(Double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getMobNumber() {
        return mobNumber;
    }

    public void setMobNumber(String mobNumber) {
        this.mobNumber = mobNumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public LocalDateTime getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDateTime invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    @Override
    public String toString() {
        return "OrderPlaced{" +
                "id=" + id +
                ", grossAmount=" + grossAmount +
                ", cgst=" + cgst +
                ", sgst=" + sgst +
                ", gst=" + gst +
                ", discount=" + discount +
                ", netAmt=" + netAmt +
                ", paidAmount=" + paidAmount +
                ", paymentMode='" + paymentMode + '\'' +
                ", paymentStatus='" + paymentStatus + '\'' +
                ", mobNumber='" + mobNumber + '\'' +
                ", username='" + username + '\'' +
                ", address='" + address + '\'' +
                ", emailId='" + emailId + '\'' +
                ", invoiceDate=" + invoiceDate +
                '}';
    }
}
