package com.RajBattery.RajBattery.Repo;


import com.RajBattery.RajBattery.Entity.ProductStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;

public interface ProductStockRepo extends JpaRepository<ProductStock,Long> {
    @Query("SELECT p.closingStk FROM ProductStock p WHERE p.productSelect = :productSelect order by p.arrivalDate desc limit 1")
    Integer findOPStockByPId(@Param("productSelect") Long productSelect);
//    @Query(value = "SELECT ps.* FROM product_stock ps JOIN (SELECT product_select, MAX(arrival_date) AS latest_arrival FROM product_stock GROUP BY product_select) AS latestStock ON ps.product_select = latestStock.product_select AND ps.arrival_date = latestStock.latest_arrival ORDER BY latestStock.latest_arrival DESC", nativeQuery = true)
//    List<ProductStock> findLatestStock(Pageable pageable);

//    @Query(value = "SELECT ps.id, ps.product_select, ps.opening_stk, ps.issue_stk, ps.return_stk, " +
//            "ps.closing_stk, ps.rec_stock, ps.user, ps.doc, ps.note " +
//            "FROM product_stock ps " +
//            "JOIN (SELECT product_select, MAX(arrival_date) AS latest_arrival " +
//            "      FROM product_stock " +
//            "      GROUP BY product_select) AS latestStock " +
//            "ON ps.product_select = latestStock.product_select " +
//            "AND ps.arrival_date = latestStock.latest_arrival " +
//            "ORDER BY latestStock.latest_arrival DESC",
//            countQuery = "SELECT COUNT(*) " +
//                    "FROM (SELECT ps.id " +
//                    "      FROM product_stock ps " +
//                    "      JOIN (SELECT product_select, MAX(arrival_date) AS latest_arrival " +
//                    "            FROM product_stock " +
//                    "            GROUP BY product_select) AS latestStock " +
//                    "      ON ps.product_select = latestStock.product_select " +
//                    "      AND ps.arrival_date = latestStock.latest_arrival) AS total",
//            nativeQuery = true)
//    Page<Object[]> findLatestStock(Pageable pageable);

    @Query("SELECT p FROM ProductStock p ORDER BY p.productSelect")
    List<ProductStock> orderByproduct_select();




}
