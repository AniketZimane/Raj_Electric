package com.RajBattery.RajBattery.Service;

import com.RajBattery.RajBattery.Entity.ProductStock;
import com.RajBattery.RajBattery.Repo.ProductStockRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StockService {

    @Autowired
    private ProductStockRepo productStockRepo;

    // ── Read ──────────────────────────────────────────────────────────────────

    /**
     * Returns the current closing stock for a product.
     * Reads the closingStk of the most-recent ledger row.
     */
    public int getCurrentStock(Long productId) {
        List<Integer> result = productStockRepo.findLatestClosingStk(
                productId, PageRequest.of(0, 1));
        return (result.isEmpty() || result.get(0) == null) ? 0 : result.get(0);
    }

    public List<ProductStock> getLedger(Long productId) {
        return productStockRepo.findLedgerByProduct(productId);
    }

    public List<ProductStock> getAllStocksOrdered() {
        return productStockRepo.orderByproduct_select();
    }

    public List<ProductStock> getCurrentStockPerProduct() {
        return productStockRepo.findCurrentStockPerProduct();
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    /**
     * RECEIPT — stock coming IN (purchase / admin add).
     * qty must be positive.
     */
    @Transactional
    public ProductStock receiveStock(Long productId, int qty, String refDoc, String note, String user) {
        if (qty <= 0) throw new IllegalArgumentException("Receipt qty must be positive");
        int opening = getCurrentStock(productId);
        return save(productId, opening, qty, "RECEIPT", refDoc, note, user);
    }

    /**
     * ISSUE — stock going OUT on an invoice.
     * qty must be positive; stored as negative signed qty internally.
     * Throws if insufficient stock.
     */
    @Transactional
    public ProductStock issueStock(Long productId, int qty, String refDoc, String note, String user) {
        if (qty <= 0) throw new IllegalArgumentException("Issue qty must be positive");
        int opening = getCurrentStock(productId);
        if (opening < qty) {
            throw new IllegalStateException(
                    "Insufficient stock for product " + productId +
                    ". Available: " + opening + ", Requested: " + qty);
        }
        return save(productId, opening, -qty, "ISSUE", refDoc, note, user);
    }

    /**
     * RETURN — stock coming back from customer.
     * qty must be positive.
     */
    @Transactional
    public ProductStock returnStock(Long productId, int qty, String refDoc, String note, String user) {
        if (qty <= 0) throw new IllegalArgumentException("Return qty must be positive");
        int opening = getCurrentStock(productId);
        return save(productId, opening, qty, "RETURN", refDoc, note, user);
    }

    /**
     * ADJUSTMENT — manual correction (can be +ve or -ve).
     */
    @Transactional
    public ProductStock adjustStock(Long productId, int signedQty, String note, String user) {
        int opening = getCurrentStock(productId);
        int closing = opening + signedQty;
        if (closing < 0) throw new IllegalStateException("Adjustment would result in negative stock");
        return save(productId, opening, signedQty, "ADJUSTMENT", null, note, user);
    }

    // ── Private helper ────────────────────────────────────────────────────────

    private ProductStock save(Long productId, int opening, int signedQty,
                               String txnType, String refDoc, String note, String user) {
        ProductStock ps = new ProductStock();
        ps.setProductSelect(productId);
        ps.setOpeningStk(opening);
        ps.setQty(signedQty);
        ps.setClosingStk(opening + signedQty);
        ps.setTxnType(txnType);
        ps.setRefDoc(refDoc);
        ps.setNote(note);
        ps.setUser(user);
        return productStockRepo.save(ps);
    }
}
