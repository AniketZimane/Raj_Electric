package com.RajBattery.RajBattery.Repo;

import com.RajBattery.RajBattery.Entity.ProductStock;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductStockRepo extends JpaRepository<ProductStock, Long> {

    /**
     * Latest closing stock for a product.
     * Uses Pageable(0,1) to get the single most-recent row — avoids non-standard LIMIT in JPQL.
     */
    @Query("SELECT p.closingStk FROM ProductStock p WHERE p.productSelect = :productId ORDER BY p.txnDate DESC")
    List<Integer> findLatestClosingStk(@Param("productId") Long productId, Pageable pageable);

    /**
     * Full ledger history for a product, newest first.
     */
    @Query("SELECT p FROM ProductStock p WHERE p.productSelect = :productId ORDER BY p.txnDate DESC")
    List<ProductStock> findLedgerByProduct(@Param("productId") Long productId);

    /**
     * All ledger rows ordered by product then date — used for stock report.
     */
    @Query("SELECT p FROM ProductStock p ORDER BY p.productSelect ASC, p.txnDate DESC")
    List<ProductStock> orderByproduct_select();

    /**
     * Latest row per product (for current-balance report).
     * Native query — gets the most recent txn_date row per product.
     */
    @Query(value = """
            SELECT ps.* FROM product_stock ps
            INNER JOIN (
                SELECT product_select, MAX(txn_date) AS latest
                FROM product_stock
                GROUP BY product_select
            ) latest_ps ON ps.product_select = latest_ps.product_select
                       AND ps.txn_date = latest_ps.latest
            ORDER BY ps.product_select
            """, nativeQuery = true)
    List<ProductStock> findCurrentStockPerProduct();
}
