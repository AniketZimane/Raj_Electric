package com.RajBattery.RajBattery.Service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartService {

    // Map to store cart data for each user
    private final Map<String, List<Map<String, Object>>> userCarts = new HashMap<>();

    public List<Map<String, Object>> getUserCart(String username) {
        return userCarts.computeIfAbsent(username, k -> new ArrayList<>());
    }

    public void addToCart(String username, Map<String, Object> productDetails) {
        List<Map<String, Object>> cart = getUserCart(username);
        boolean exists = cart.stream().anyMatch(p -> p.get("ProdId").equals(productDetails.get("ProdId")));
        if (!exists) {
            cart.add(productDetails);
        }
    }

    public void removeFromCart(String username, Long productId) {
        List<Map<String, Object>> cart = getUserCart(username);
        cart.removeIf(product -> product.get("ProdId").equals(productId));
    }
}
