package com.RajBattery.RajBattery.Controller;

import com.RajBattery.RajBattery.Entity.*;
import com.RajBattery.RajBattery.Repo.*;
import com.RajBattery.RajBattery.Service.CartService;
import com.RajBattery.RajBattery.Service.OfferService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class UserController {

    @Autowired
    HttpSession session;
    @Autowired
    ProductRepo productRepo;
    @Autowired
    OfferRepository offerRepository;
    @Autowired
    CategoryRepo categoryRepo;
    @Autowired
    CompanyDetailsRepo companyDetailsRepo;
    @Autowired
    ProductStockRepo productStockRepo;
    @Autowired
    OfferService offerService;
    @Autowired
    CartService cartService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    RegistrationRepo registrationRepo;
    List<Map<String, Object>> cart;

    public void companyInfo(Model model)
    {
        List<CompanyDetails> companyDetails=companyDetailsRepo.findAll();
        for (CompanyDetails comp:companyDetails) {
            model.addAttribute("Cname",comp.getcName());
        }
    }
    @GetMapping("/admin/dashboard/")

    public String getHome(Model model)
    {
        List<CompanyDetails> companyDetails=companyDetailsRepo.findAll();
        for (CompanyDetails comp:companyDetails) {
            model.addAttribute("Cname",comp.getcName());
        }
        return "adminDashboard";
    }


    @GetMapping("/new/Product/")
    public String getNewProduct(Model model)
    {
        List<Category> categoryList=categoryRepo.findAll();
        model.addAttribute("categoryList",categoryList);
        List<CompanyDetails> companyDetails=companyDetailsRepo.findAll();
        companyInfo(model);
        return "ProductForm";
    }
    @GetMapping("/home/")
    public String getHomePage(Model model)
    {
        List<Product> productList=productRepo.findAll();
        List<Offer> offersData = offerService.getValidOffers();
        model.addAttribute("offersData",offersData);
        model.addAttribute("productList",productList);
        return "home";
    }

    @GetMapping("/user/products/")
    public String getProductPage(Model model)
    {
        List<Product> productList = productRepo.findAll();
        model.addAttribute("productList",productList);
        return "Products";
    }

    @GetMapping("/user/cart/")
    public String getCartPage(Model model)
    {
        String page="CartPage";
        if(cart!=null)
        {
            model.addAttribute("productData",cart);
        }else{
            List<Product> productList = productRepo.findAll();
            model.addAttribute("productList",productList);
            model.addAttribute("emsg","No product In your cart!");
            page="Products";
        }
        return page;
    }

    @GetMapping("/user/customer/invoice/")
    public String getCustomerInvoice()
    {
        return "CustomerInvoice";
    }

    @GetMapping("/user/customer/details/")
    public String getCustomerDetails()
    {
        return "CustomerDetails";
    }

    @GetMapping("/login/")
    public String getLoginPage(Model model)
    {
        return "Login";
    }

    @GetMapping("/logout/")
    public String getLogOutPage()
    {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/signup/")
    public String getRegistrationPage()
    {
        session.invalidate();
        return "signup";
    }
    @PostMapping("/user/register/")
    public String RegisterUser(Registration reg,Model model)
    {

        String page="signup";
        Registration existingUser=registrationRepo.getByEmail(reg.getEmail());
        if(existingUser!=null)
        {
            model.addAttribute("emsg","User already exist!");
            page="Login";
        }
        else{
            String encryptedPassword = passwordEncoder.encode(reg.getPassword());
            reg.setPassword(encryptedPassword);
            reg.setUserType("0");
            registrationRepo.save(reg);
            model.addAttribute("msg","User register successfully");
            page="Login";
        }
        return page;
    }



    @GetMapping("/user/buy/product/{id}/")
    public String getProduct(@PathVariable("id") Long id, Model model) {
        String username=(String)session.getAttribute("key");
        Optional<Product> productOptional = productRepo.findById(id);
        Map<String, Object> productData = new HashMap<>();
        System.out.println(productOptional);
        if (productOptional.isPresent()) {
            Product data=productOptional.get();
            Integer stock=productStockRepo.findOPStockByPId(data.getPId());
            productData.put("ProdId",data.getPId());
            productData.put("pName",data.getPName());
            productData.put("pPrize",data.getPrize());
            productData.put("pStock",stock);
            productData.put("extension",data.getExtension());
            productData.put("username",username);
            model.addAttribute("productData",productData);
        } else {
            model.addAttribute("errorMessage", "Product not found!");
        }

        return "CartPage";
    }

    @GetMapping("/user/add/product/{id}/")
    public String addToCartProduct(@PathVariable("id") Long id,

                                   Model model) {
        Optional<Product> productOptional = productRepo.findById(id);

        String username=(String)session.getAttribute("key");
        if (productOptional.isPresent()) {
            Product data = productOptional.get();
            Integer stock = productStockRepo.findOPStockByPId(data.getPId());

            Map<String, Object> productDetails = new HashMap<>();
            productDetails.put("ProdId", data.getPId());
            productDetails.put("pName", data.getPName());
            productDetails.put("pPrize", data.getPrize());

            productDetails.put("pStock", stock);
            productDetails.put("extension", data.getExtension());
            productDetails.put("username", username);

            cartService.addToCart(username, productDetails);

            model.addAttribute("msg", "Product added successfully!");
        } else {
            model.addAttribute("errorMessage", "Product not found!");
        }

        return "redirect:/user/view/cart";
    }

    @GetMapping("/user/view/cart")
    public String viewCart(Model model) {
        String username=(String)session.getAttribute("key");
        cart = cartService.getUserCart(username);
        System.out.println(cart);
        List<Product> productList = productRepo.findAll();
        model.addAttribute("productList",productList);
        model.addAttribute("cartData", cart);
        return "Products";
    }

    @PostMapping("/user/remove/product/{id}/")
    public String removeFromCart(@PathVariable("id") Long id,
                                 Model model) {
        String username=(String)session.getAttribute("key");
        cartService.removeFromCart(username, id);
        return "redirect:/user/view/cart";
    }

    @PutMapping("/user/update/procureQty/{id}/{quantity}")
    @ResponseBody
    public ResponseEntity<String> updateProcureQty(@PathVariable("id") Long id, @PathVariable("quantity") Integer quantity) {
        boolean productFound = false;
        System.out.println("idcart"+cart);
        for (Map<String, Object> productData : cart) {
            if (productData.get("ProdId").equals(id)) { // Check if product exists in the cart
                productData.put("procureQty", quantity); // Update the quantity
                productFound = true;
                break;
            }
        }

        if (productFound) {
            System.out.println("updated cart"+cart);
            return ResponseEntity.ok("Quantity updated successfully!");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found in cart!");
        }
    }

    @GetMapping("/user/order/placed/")
    public String userOrderPlaced(Model model)
    {
        String custName=session.getAttribute("key").toString();
        List<CompanyDetails> companyDetails=companyDetailsRepo.findAll();
        for (CompanyDetails comp:companyDetails) {
            model.addAttribute("Cname",comp.getcName());
            model.addAttribute("Cadd",comp.getcAddress());
            model.addAttribute("Cnumber",comp.getcNumber());
            model.addAttribute("gstNumber",comp.getGstNumber());
        }


//        model.addAttribute("id", id);
//        model.addAttribute("grossAmount", grossAmount);
//        model.addAttribute("username", userName);
//        model.addAttribute("cgst", cgst);
//        model.addAttribute("sgst", sgst);
//        model.addAttribute("gst", gst);
//        model.addAttribute("discount", discount);
//        model.addAttribute("netAmt", netAmt);
//        model.addAttribute("paidAmount", paidAmount);
//        model.addAttribute("paymentMode", paymentMode);
//        model.addAttribute("paymentStatus", paymentStatus);
//        model.addAttribute("mobNumber", mobNumber);
//        model.addAttribute("emailId", emaiId);
//        model.addAttribute("address", address);
        model.addAttribute("extractData", cart);
//        model.addAttribute("invoiceDate", invoiceDate);

        return "Invoice"; // This will render the Invoice template
    }

    @GetMapping("/loader/")
    public String getLoader(Model model)
    {
        return "loader";
    }







}
