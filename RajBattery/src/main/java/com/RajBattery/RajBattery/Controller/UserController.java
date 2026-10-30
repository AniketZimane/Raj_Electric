package com.RajBattery.RajBattery.Controller;

import com.RajBattery.RajBattery.Entity.Registration;
import com.RajBattery.RajBattery.Service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class UserController {

    @Autowired private HttpSession session;
    @Autowired private ProductService productService;
    @Autowired private CartService cartService;
    @Autowired private CompanyService companyService;
    @Autowired private UserService userService;
    @Autowired private StockService stockService;

    private List<Map<String, Object>> cart;

    @GetMapping("/")
    public String redirectToLogin() { return "redirect:/login"; }

    @GetMapping("/login")
    public String getLoginPage(Model model) {
        return "login";
    }

    @GetMapping("/signup")
    public String getRegistrationPage(Model model) {
        session.invalidate();
        return "signup";
    }

    @GetMapping("/logout")
    public String getLogOutPage() {
        session.invalidate();
        return "redirect:/";
    }

    @PostMapping("/user/register/")
    public String registerUser(Registration reg, Model model) {
        if (userService.existsByEmail(reg.getEmail())) {
            model.addAttribute("emsg", "User already exist!");
        } else {
            userService.register(reg);
            model.addAttribute("msg", "User register successfully");
        }
        return "login";
    }

    @GetMapping("/user/products")
    public String getProductPage(Model model) {
        model.addAttribute("productList", productService.getAll());
        return "Products";
    }

    @GetMapping("/user/buy/product/{id}/")
    public String getProduct(@PathVariable Long id, Model model) {
        String username = (String) session.getAttribute("key");
        return productService.findById(id).map(data -> {
            Map<String, Object> productData = new HashMap<>();
            productData.put("ProdId", data.getPId());
            productData.put("pName", data.getPName());
            productData.put("pPrize", data.getPrize());
            productData.put("pStock", stockService.getCurrentStock(data.getPId()));
            productData.put("extension", data.getExtension());
            productData.put("username", username);
            model.addAttribute("productData", productData);
            return "CartPage";
        }).orElseGet(() -> {
            model.addAttribute("errorMessage", "Product not found!");
            return "CartPage";
        });
    }

    @GetMapping("/user/add/product/{id}/")
    public String addToCartProduct(@PathVariable Long id, Model model) {
        String username = (String) session.getAttribute("key");
        productService.findById(id).ifPresent(data -> {
            Map<String, Object> details = new HashMap<>();
            details.put("ProdId", data.getPId());
            details.put("pName", data.getPName());
            details.put("pPrize", data.getPrize());
            details.put("pStock", stockService.getCurrentStock(data.getPId()));
            details.put("extension", data.getExtension());
            details.put("username", username);
            cartService.addToCart(username, details);
        });
        return "redirect:/user/view/cart";
    }

    @GetMapping("/user/view/cart")
    public String viewCart(Model model) {
        String username = (String) session.getAttribute("key");
        cart = cartService.getUserCart(username);
        model.addAttribute("productList", productService.getAll());
        model.addAttribute("cartData", cart);
        return "Products";
    }

    @GetMapping("/user/cart/")
    public String getCartPage(Model model) {
        if (cart != null) {
            model.addAttribute("productData", cart);
            return "CartPage";
        }
        model.addAttribute("productList", productService.getAll());
        model.addAttribute("emsg", "No product In your cart!");
        return "Products";
    }

    @PostMapping("/user/remove/product/{id}/")
    public String removeFromCart(@PathVariable Long id) {
        cartService.removeFromCart((String) session.getAttribute("key"), id);
        return "redirect:/user/view/cart";
    }

    @PutMapping("/user/update/procureQty/{id}/{quantity}")
    @ResponseBody
    public ResponseEntity<String> updateProcureQty(@PathVariable Long id, @PathVariable Integer quantity) {
        if (cart == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cart is empty!");
        for (Map<String, Object> item : cart) {
            if (item.get("ProdId").equals(id)) {
                item.put("procureQty", quantity);
                return ResponseEntity.ok("Quantity updated successfully!");
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found in cart!");
    }

    @GetMapping("/user/order/placed/")
    public String userOrderPlaced(Model model) {
        companyService.populateCompanyInfo(model);
        model.addAttribute("extractData", cart);
        return "Invoice";
    }

    @GetMapping("/user/customer/invoice/")
    public String getCustomerInvoice() { return "CustomerInvoice"; }

    @GetMapping("/user/customer/details/")
    public String getCustomerDetails() { return "CustomerDetails"; }

    @GetMapping("/loader")
    public String getLoader() { return "loader"; }
}
