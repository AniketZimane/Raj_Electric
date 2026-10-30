package com.RajBattery.RajBattery.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

/**
 * Ledger-style stock table.
 * Each row = one stock transaction (RECEIPT / ISSUE / RETURN / ADJUSTMENT).
 *
 * Schema logic:
 *   closingStk = openingStk + qty  (where qty is signed: +ve for IN, -ve for OUT)
 *
 * txnType values:
 *   RECEIPT    – stock received from supplier / purchase order
 *   ISSUE      – stock issued on inhouse invoice (sale)
 *   RETURN     – stock returned by customer
 *   ADJUSTMENT – manual correction by admin
 */
@Entity
@Table(name = "product_stock",
       indexes = {
           @Index(name = "idx_ps_product",  columnList = "product_select"),
           @Index(name = "idx_ps_txn_date", columnList = "txn_date")
       })
public class ProductStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_select", nullable = false)
    private Long productSelect;

    /** Stock balance before this transaction */
    @Column(nullable = false)
    private Integer openingStk;

    /**
     * Signed quantity for this transaction.
     * +ve = stock IN (receipt / return), -ve = stock OUT (issue)
     */
    @Column(nullable = false)
    private Integer qty;

    /** Stock balance after this transaction = openingStk + qty */
    @Column(nullable = false)
    private Integer closingStk;

    /** RECEIPT | ISSUE | RETURN | ADJUSTMENT */
    @Column(name = "txn_type", nullable = false, length = 20)
    private String txnType;

    /** Reference document (Invoice no, PO no, etc.) */
    @Column(name = "ref_doc")
    private String refDoc;

    private String note;
    private String user;

    @CreationTimestamp
    @Column(name = "txn_date", updatable = false)
    private LocalDateTime txnDate;

    public ProductStock() {}

    // ── Getters & Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductSelect() { return productSelect; }
    public void setProductSelect(Long productSelect) { this.productSelect = productSelect; }

    public Integer getOpeningStk() { return openingStk; }
    public void setOpeningStk(Integer openingStk) { this.openingStk = openingStk; }

    public Integer getQty() { return qty; }
    public void setQty(Integer qty) { this.qty = qty; }

    public Integer getClosingStk() { return closingStk; }
    public void setClosingStk(Integer closingStk) { this.closingStk = closingStk; }

    public String getTxnType() { return txnType; }
    public void setTxnType(String txnType) { this.txnType = txnType; }

    public String getRefDoc() { return refDoc; }
    public void setRefDoc(String refDoc) { this.refDoc = refDoc; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    public LocalDateTime getTxnDate() { return txnDate; }
    public void setTxnDate(LocalDateTime txnDate) { this.txnDate = txnDate; }

    // ── Legacy compatibility getters (used by existing templates/reports) ────

    /** @deprecated use getQty() — kept for template compatibility */
    public Integer getRecStock()  { return txnType != null && txnType.equals("RECEIPT") ? qty : 0; }
    /** @deprecated use getQty() — kept for template compatibility */
    public Integer getIssueStk()  { return txnType != null && txnType.equals("ISSUE")   ? Math.abs(qty) : 0; }
    /** @deprecated use getQty() — kept for template compatibility */
    public Integer getReturnStk() { return txnType != null && txnType.equals("RETURN")  ? qty : 0; }
    /** @deprecated use getRefDoc() — kept for template compatibility */
    public String  getDoc()       { return refDoc; }

    @Override
    public String toString() {
        return "ProductStock{id=" + id + ", product=" + productSelect +
               ", txnType='" + txnType + "', qty=" + qty +
               ", opening=" + openingStk + ", closing=" + closingStk + "}";
    }
}
