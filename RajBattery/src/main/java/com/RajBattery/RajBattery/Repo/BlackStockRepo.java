package com.RajBattery.RajBattery.Repo;

import com.RajBattery.RajBattery.Entity.BlackStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BlackStockRepo extends JpaRepository<BlackStock,Long> {
    @Query("SELECT p.closingStk FROM ProductStock p WHERE p.productSelect = :productSelect order by p.arrivalDate desc limit 1")
    Integer findOPStockByPId(@Param("productSelect") Long productSelect);
}
