package com.RajBattery.RajBattery.Controller;

import com.RajBattery.RajBattery.Entity.Product;
import com.RajBattery.RajBattery.Service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/category")
public class CategoryController {

    @Autowired private ProductService productService;

    @PostMapping("/")
    public ResponseEntity<?> getProductsByCategory(@RequestBody Map<String, Long> requestData) {
        Long categoryId = requestData.get("categoryId");
        if (categoryId == null) return ResponseEntity.badRequest().body("Category ID is required");
        List<Product> list = productService.findByCategory(categoryId.toString());
        return list.isEmpty()
                ? ResponseEntity.status(HttpStatus.NOT_FOUND).body("No products found.")
                : ResponseEntity.ok(list);
    }
}
