package com.RajBattery.RajBattery.Repo;

import com.RajBattery.RajBattery.Entity.PurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PurchaseOrderRepo extends JpaRepository<PurchaseOrder, Long> {

    Page<PurchaseOrder> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<PurchaseOrder> findByVendorId(Long vendorId);

    List<PurchaseOrder> findByStatus(String status);

    Optional<PurchaseOrder> findByPoNumber(String poNumber);

    @Query("SELECT COUNT(p) FROM PurchaseOrder p WHERE p.status = :status")
    Long countByStatus(@Param("status") String status);
}
