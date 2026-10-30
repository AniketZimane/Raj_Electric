package com.RajBattery.RajBattery.Repo;

import com.RajBattery.RajBattery.Entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VendorRepo extends JpaRepository<Vendor, Long> {

    List<Vendor> findByActiveTrue();

    Optional<Vendor> findByEmail(String email);

    List<Vendor> findByVendorNameContainingIgnoreCase(String name);

    @Query("SELECT v FROM Vendor v WHERE v.active = true AND LOWER(v.vendorName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Vendor> searchActive(@Param("query") String query);

    boolean existsByEmail(String email);
}
