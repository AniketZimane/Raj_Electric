package com.RajBattery.RajBattery.Service;

import com.RajBattery.RajBattery.Entity.Product;
import com.RajBattery.RajBattery.Entity.Vendor;
import com.RajBattery.RajBattery.Repo.ProductRepo;
import com.RajBattery.RajBattery.Repo.VendorRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class VendorService {

    @Autowired private VendorRepo vendorRepo;
    @Autowired private ProductRepo productRepo;

    public List<Vendor> getAllActive() {
        return vendorRepo.findByActiveTrue();
    }

    public List<Vendor> getAll() {
        return vendorRepo.findAll();
    }

    public Optional<Vendor> findById(Long id) {
        return vendorRepo.findById(id);
    }

    public Vendor save(Vendor vendor) {
        return vendorRepo.save(vendor);
    }

    public void deactivate(Long id) {
        vendorRepo.findById(id).ifPresent(v -> { v.setActive(false); vendorRepo.save(v); });
    }

    public boolean emailExists(String email) {
        return vendorRepo.existsByEmail(email);
    }

    public List<Vendor> search(String query) {
        return vendorRepo.searchActive(query);
    }

    /**
     * For each item name in the request, check if it exists in the product DB.
     * Returns a map: productName -> { matched, productId, suggestions (if unmatched) }
     */
    public List<Map<String, Object>> matchProductsToVendors(List<String> productNames) {
        List<Map<String, Object>> results = new ArrayList<>();

        for (String name : productNames) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("requestedName", name);

            // Exact match first
            List<Product> exactMatches = productRepo.findByPName(name.trim());
            if (!exactMatches.isEmpty()) {
                Product p = exactMatches.get(0);
                result.put("matched", true);
                result.put("productId", p.getPId());
                result.put("productName", p.getPName());
                result.put("unitRate", p.getPrize());
                result.put("suggestions", Collections.emptyList());
            } else {
                // Fuzzy search — find similar names
                List<Product> similar = productRepo.findByPNameContainingIgnoreCase(name.trim());
                List<Map<String, Object>> suggestions = similar.stream().limit(5).map(p -> {
                    Map<String, Object> s = new LinkedHashMap<>();
                    s.put("productId", p.getPId());
                    s.put("productName", p.getPName());
                    s.put("unitRate", p.getPrize());
                    return s;
                }).collect(Collectors.toList());

                result.put("matched", false);
                result.put("productId", null);
                result.put("productName", name);
                result.put("unitRate", 0.0);
                result.put("suggestions", suggestions);
            }
            results.add(result);
        }
        return results;
    }
}
