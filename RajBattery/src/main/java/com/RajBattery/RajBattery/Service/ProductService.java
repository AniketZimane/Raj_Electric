package com.RajBattery.RajBattery.Service;

import com.RajBattery.RajBattery.Entity.Product;
import com.RajBattery.RajBattery.Repo.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private FileUploader uploader;

    public List<Product> getAll() {
        return productRepo.findAll();
    }

    public Optional<Product> findById(Long id) {
        return productRepo.findById(id);
    }

    public Product getById(Long id) {
        return productRepo.getReferenceById(id);
    }

    public Page<Product> getPaginated(Pageable pageable) {
        return productRepo.findAll(pageable);
    }

    public void deleteById(Long id) {
        productRepo.deleteById(id);
    }

    public boolean existsByName(String name) {
        return !productRepo.findByPName(name).isEmpty();
    }

    public void saveWithFile(Product product, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            String original = file.getOriginalFilename();
            if (original == null || !original.contains(".")) throw new IllegalArgumentException("Invalid file format.");
            String ext = original.substring(original.lastIndexOf('.') + 1);
            product.setExtension(ext);
            Product saved = productRepo.save(product);
            uploader.uploadFile(file, saved.getPId() + "." + ext);
        } else {
            productRepo.save(product);
        }
    }

    public List<Map<String, Object>> searchByName(String query) {
        return productRepo.findByPNameContainingIgnoreCase(query)
                .stream()
                .sorted(Comparator.comparing(Product::getPName, String.CASE_INSENSITIVE_ORDER))
                .map(p -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", p.getPId());
                    m.put("name", p.getPName());
                    m.put("prize", p.getPrize());
                    return m;
                }).collect(Collectors.toList());
    }

    public Map<String, Object> getProductMapById(Long id) {
        return productRepo.findById(id).map(p -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getPId());
            m.put("name", p.getPName());
            m.put("prize", p.getPrize());
            return m;
        }).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public List<Product> findByCategory(String categoryId) {
        return productRepo.findByCategory(categoryId);
    }
}
