package com.RajBattery.RajBattery.Repo;

import com.RajBattery.RajBattery.Entity.Category;
import com.RajBattery.RajBattery.Entity.Product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepo extends JpaRepository<Product,Long> {
    List<Product> findByPName(String PName);

    List<Product> findByCategory(String category);

    @Query("SELECT p.PName FROM Product p WHERE p.PId = :PId")
    String findPNameByPId(@Param("PId") Long PId);

    @Query("SELECT p.prize FROM Product p WHERE p.PId = :PId")
    Double findPrizeByPId(@Param("PId") Long PId);

    
}
