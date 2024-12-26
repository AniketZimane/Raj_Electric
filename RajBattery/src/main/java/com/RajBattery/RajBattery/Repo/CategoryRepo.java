package com.RajBattery.RajBattery.Repo;

import com.RajBattery.RajBattery.Entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepo extends JpaRepository<Category,Long> {
    List<Category> findByCate(String cate);

}
