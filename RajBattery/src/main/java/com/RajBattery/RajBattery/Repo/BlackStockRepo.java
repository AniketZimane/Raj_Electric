package com.RajBattery.RajBattery.Repo;

import com.RajBattery.RajBattery.Entity.BlackStock;
import org.springframework.data.jpa.repository.JpaRepository;

// Black stock logic removed — all stock is now tracked via ProductStock ledger.
public interface BlackStockRepo extends JpaRepository<BlackStock, Long> {
}
