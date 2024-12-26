package com.RajBattery.RajBattery.Controller;

import com.RajBattery.RajBattery.Entity.*;
import com.RajBattery.RajBattery.Repo.*;
import com.RajBattery.RajBattery.Service.FileUploader;
import com.RajBattery.RajBattery.Service.OfferService;

import com.RajBattery.RajBattery.Service.OrderPlacedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;



@Controller
public class AdminController {
    int maxSize=10;
    @Autowired
    FileUploader uploader;
    @Autowired
    ProductRepo productRepo;
    @Autowired
    CategoryRepo categoryRepo;
    @Autowired
    InhouseInvoiceRepo inhouseInvoiceRepo;
    @Autowired
    OrderPlacedRepo orderPlacedRepo;
    @Autowired
    ProductStockRepo productStockRepo;
    @Autowired
    CompanyDetailsRepo companyDetailsRepo;
    @Autowired
    OfferService offerService;
    @Autowired
    RegistrationRepo registrationRepo;
    @Autowired
    OrderPlacedService orderPlacedService;
    @Autowired
    BlackStockRepo blackStockRepo;

    public void companyInfo(Model model)
    {
        List<CompanyDetails> companyDetails=companyDetailsRepo.findAll();
        for (CompanyDetails comp:companyDetails) {
            model.addAttribute("Cname",comp.getcName());
        }
    }

    @GetMapping("/admin/dashboard/")
    public String getHome(Model model) {
        // Fetch all products and calculate total count
        List<Product> productList = productRepo.findAll();
        int totalProduct = (productList != null) ? productList.size() : 0;  // Null check for the product list

        // Fetch company details and add company name to the model
        List<CompanyDetails> companyDetailsList = companyDetailsRepo.findAll();
        if (companyDetailsList != null && !companyDetailsList.isEmpty()) {
            CompanyDetails companyDetails = companyDetailsList.get(0); // Assuming only one company detail
            model.addAttribute("Cname", companyDetails.getcName());
        } else {
            model.addAttribute("Cname", "Unknown Company");  // Fallback if company details are missing
        }

        // Fetch and calculate financial details
        Double totalNetAmt = orderPlacedRepo.sumOfNetAmt();
        Double totalPaidAmt = orderPlacedRepo.sumOfPaidAmount();

        // Ensure null safety for financial calculations
        totalNetAmt = (totalNetAmt != null) ? totalNetAmt : 0.0;
        totalPaidAmt = (totalPaidAmt != null) ? totalPaidAmt : 0.0;
        Double remainingAmt = totalNetAmt - totalPaidAmt;

        // Fetch online customer count safely (handle if it returns null)
        Integer onlineCustomerCount = registrationRepo.findCountByUserType();
        onlineCustomerCount = (onlineCustomerCount != null) ? onlineCustomerCount : 0;  // Null check for customer count

        // Add attributes to the model
        model.addAttribute("totalProduct", totalProduct);
        model.addAttribute("onlineCustomer", onlineCustomerCount);
        model.addAttribute("totalNetAmt", String.format("%.3f", totalNetAmt)); // Format to 3 decimal points
        model.addAttribute("remainingAmt", String.format("%.3f", remainingAmt)); // Format to 3 decimal points

        // Return the view name
        return "adminDashboard";
    }




    @PostMapping("/admin/addnewproduct/")
    public String handleProductData(Model model, Product product, MultipartFile file) {
        String redirectPage = "ProductForm";

        try {

            if (product.getPId() == null) {

                List<Product> existingProductName = productRepo.findByPName(product.getPName());

                if (!existingProductName.isEmpty()) {
                    model.addAttribute("emsg", "Product already exists.");
                    companyInfo(model);
                    return redirectPage;
                }


                handleFileAndSaveProduct(product, file);
                companyInfo(model);
                model.addAttribute("msg", "Product registration successfully completed.");
            } else {

                Optional<Product> existingProduct = productRepo.findById(product.getPId());

                if (existingProduct==null) {
                    model.addAttribute("emsg", "Product not found for update.");
                    companyInfo(model);
                    return redirectPage;
                }

                handleFileAndSaveProduct(product, file);
                companyInfo(model);
                model.addAttribute("msg", "Product updated successfully.");
            }
        } catch (Exception e) {
            model.addAttribute("emsg", "An error occurred while processing the request.");
            e.printStackTrace();
        }

        return redirectPage;
    }


    private void handleFileAndSaveProduct(Product product, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            String fileNameOld = file.getOriginalFilename();
            if (fileNameOld == null || !fileNameOld.contains(".")) {
                throw new IllegalArgumentException("Invalid file format.");
            }

            String extension = fileNameOld.substring(fileNameOld.lastIndexOf('.') + 1);
            product.setExtension(extension);

            Product savedProduct = productRepo.save(product);

            String fileNameNew = savedProduct.getPId() + "." + extension;

            uploader.uploadFile(file, fileNameNew);
        } else {
            productRepo.save(product);
        }
    }

    @GetMapping("/Product/report/")
    public String getProductReport(Model model)
    {

        int curPage = 1;

        Pageable pageable = PageRequest.of(curPage, maxSize, Sort.by("PId").descending());
        Page<Product> page = productRepo.findAll(pageable);
        int totalPages = page.getTotalPages();
        List<Product> ProductRequestList = page.toList();
        List<Product> productL1 = productRepo.findAll();
        model.addAttribute("curPage", curPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("productList", ProductRequestList);
        model.addAttribute("productList", productL1);
        model.addAttribute("recordSize", productL1.size());
        companyInfo(model);
        return "ProductReport";
    }

    @GetMapping("/admin/delete/product/{PId}/")
    public String deleteProductRecord(Model model, @PathVariable Long PId) {
        productRepo.deleteById(PId);
        int curPage = 1;
        Pageable pageable = PageRequest.of(curPage, maxSize, Sort.by("PId").descending());
        Page<Product> page = productRepo.findAll(pageable);
        int totalPages = page.getTotalPages();
        List<Product> productList = page.toList();
        List<Product> productList1 = productRepo.findAll();
        model.addAttribute("resultList", productList);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("curPage", curPage);
        model.addAttribute("productList", productList);
        model.addAttribute("recordSize", productList.size());
        model.addAttribute("msg", "Record Deleted");
        companyInfo(model);
        return "ProductReport";
    }

    @GetMapping("/admin/edit/product/{PId}/")
    public String updateProductDetails(Model model,@PathVariable Long PId)
    {
        List<Category> categoryList=categoryRepo.findAll();
        Product data=productRepo.getReferenceById(PId);
        model.addAttribute("data",data);
        model.addAttribute("categoryList",categoryList);
        companyInfo(model);
        return "ProductForm";
    }

    @GetMapping("/admin/delete/invoice/{id}/")
    public String deleteInvoice(Model model,@PathVariable Long id, @RequestParam(defaultValue = "1") int curPage,
                                @RequestParam(defaultValue = "8") int size
                                )
    {
        List<InhouseInvoice> inhouseInvoices=inhouseInvoiceRepo.findByOrderId(id);
        System.out.println(inhouseInvoices);
        orderPlacedRepo.deleteById(id);
        companyInfo(model);
        // Adjust for 0-based indexing
        curPage = Math.max(curPage, 1); // Ensure curPage is at least 1

        Pageable pageable = PageRequest.of(curPage - 1, size, Sort.by("id").descending());

        Page<OrderPlaced> paginatedOrders = orderPlacedRepo.findAll(pageable);

        // Ensure curPage does not exceed total pages
        if (curPage > paginatedOrders.getTotalPages()) {
            curPage = paginatedOrders.getTotalPages();
            pageable = PageRequest.of(curPage - 1, size, Sort.by("id").descending());
            paginatedOrders = orderPlacedRepo.findAll(pageable);
        }
        Map<String, Double> aggregates = orderPlacedService.getAggregateAmounts();
        model.addAttribute("curPage", curPage);
        model.addAttribute("totalPages", paginatedOrders.getTotalPages());
        model.addAttribute("orderPlacedList", paginatedOrders.getContent());
        model.addAttribute("recordSize", paginatedOrders.getTotalElements());
        model.addAttribute("totalNetAmt", aggregates.get("totalNetAmt"));
        model.addAttribute("totalPaidAmt", aggregates.get("totalPaidAmt"));
        model.addAttribute("totalDueAmt", aggregates.get("totalDueAmt"));
        return "InhouseSellReport";
    }


    @GetMapping("/admin/update/offer/")
    public String setOffer(Model model)
    {

        return "ProductOffer";
    }

    @GetMapping("/admin/inhouseInvoice/report/")
    public String getInhouseInvoiceReport(
            @RequestParam(defaultValue = "1") int curPage,
            @RequestParam(defaultValue = "8") int size,
            Model model) {
        companyInfo(model);
        // Adjust for 0-based indexing
        curPage = Math.max(curPage, 1); // Ensure curPage is at least 1

        Pageable pageable = PageRequest.of(curPage - 1, size, Sort.by("id").descending());

        Page<OrderPlaced> paginatedOrders = orderPlacedRepo.findAll(pageable);

        // Ensure curPage does not exceed total pages
        if (curPage > paginatedOrders.getTotalPages()) {
            curPage = paginatedOrders.getTotalPages();
            pageable = PageRequest.of(curPage - 1, size, Sort.by("id").descending());
            paginatedOrders = orderPlacedRepo.findAll(pageable);
        }
        Map<String, Double> aggregates = orderPlacedService.getAggregateAmounts();
        model.addAttribute("curPage", curPage);
        model.addAttribute("totalPages", paginatedOrders.getTotalPages());
        model.addAttribute("orderPlacedList", paginatedOrders.getContent());
        model.addAttribute("recordSize", paginatedOrders.getTotalElements());
        model.addAttribute("totalNetAmt", aggregates.get("totalNetAmt"));
        model.addAttribute("totalPaidAmt", aggregates.get("totalPaidAmt"));
        model.addAttribute("totalDueAmt", aggregates.get("totalDueAmt"));

        return "InhouseSellReport";
    }




    @GetMapping("/admin/onlineSell/report/")
    public String getonlineInvoiceREport(Model model)
    {
        companyInfo(model);
        return "OnlineSellingReport";
    }

    @GetMapping("/admin/create/category/")
    public String setCategory(Model model)
    {
        companyInfo(model);
        return "ProductCategory";
    }

    @GetMapping("/admin/inhouse/invoice/")
    public String setInhouceInvoice(Model model)
    {
        companyInfo(model);
        List<Category> categoryList=categoryRepo.findAll();
        model.addAttribute("categoryList",categoryList);
        return "InhouseInvoice";
    }
    @GetMapping("/admin/black-inhouse/invoice/")
    public String getInhouceInvoice(Model model)
    {
        companyInfo(model);
        List<Category> categoryList=categoryRepo.findAll();
        model.addAttribute("categoryList",categoryList);
        return "InhouseInvoiceBlack";
    }

    @GetMapping("/admin/product/stock/")
    public String setproductStock(Model model)
    {

        List<Category> categoryList=categoryRepo.findAll();
        model.addAttribute("categoryList",categoryList);
        companyInfo(model);
        return "ProductStock";
    }

    @PostMapping("/admin/saved/category/")
    public String savedCategory(Model model, Category category) {


        List<Category> categoryList = categoryRepo.findByCate(category.getCate().toString());
        if (categoryList.isEmpty()) {
            categoryRepo.save(category);
            companyInfo(model);
            model.addAttribute("msg", "Category saved successfully");
        } else {
            companyInfo(model);
            model.addAttribute("emsg", "Category already exists");
        }
        return "ProductCategory"; // Ensure this maps to the correct view template
    }

    @Controller
    @RequestMapping("/offers")
    public class OfferController {
        @Autowired
        private OfferService offerService;

        @PostMapping("/upload")
        public String uploadOffer(Model model,
                                  @RequestParam("offerName") String offerName,
                                  @RequestParam("description") String description,
                                  @RequestParam("endDate") String endDate,
                                  @RequestParam("banner") MultipartFile banner) {
            try {
                Offer offer = new Offer();
                offer.setOfferName(offerName);
                offer.setDescription(description);
                offer.setEndDate(endDate);
                offer.setBanner(banner.getBytes()); // Convert MultipartFile to byte array
                offerService.saveOffer(offer); // Save to database
                model.addAttribute("msg", "Offer uploaded successfully");
                companyInfo(model);
            } catch (Exception e) {
                companyInfo(model);
                model.addAttribute("emsg", "Banner size is too large");
            }
            return "ProductOffer"; // Returns the "ProductOffer" page
        }

        @GetMapping("/{id}/banner")
        public ResponseEntity<byte[]> getOfferBanner(@PathVariable Long id) {

            Offer offer = offerService.getOfferById(id);
            if (offer == null || offer.getBanner() == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(offer.getBanner());
        }
    }

    @RestController
    @RequestMapping("/admin/category")
    public class CategoryController {

        @Autowired
        private ProductRepo productRepo;

        @PostMapping("/")
        public ResponseEntity<?> updateCategory(@RequestBody Map<String, Long> requestData) {
            Long categoryId = requestData.get("categoryId");

            if (categoryId == null) {
                return ResponseEntity.badRequest().body("Category ID is required");
            }
            List<Product> productList = productRepo.findByCategory(requestData.get("categoryId").toString());
            if (productList.isEmpty()) {

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No products found for the specified category.");
            }

            return ResponseEntity.ok(productList);
        }
    }


    @PostMapping("/save-item/")
    public ResponseEntity<Map<String,Object>> saveItem(@RequestBody Map<String, Object> requestData,Model model) {
        try {
            String status=null;
            Long productId = Long.valueOf(requestData.get("productId").toString());
            Integer categoryId = Integer.valueOf(requestData.get("categoryId").toString());
            Integer quantity = Integer.valueOf(requestData.get("quantity").toString());
            Double rate = Double.valueOf(requestData.get("rate").toString());
            Double totalAmount = Double.valueOf(requestData.get("totalAmount").toString());
            String userName = requestData.get("userName").toString();
            String  userAddress = requestData.get("userAddress").toString();
            String  seriesNo = requestData.get("seriesNo").toString();
            // Extract and set default values if the data is missing
            Float oldWeight = requestData.containsKey("OldWight") && requestData.get("OldWight") != null && !requestData.get("OldWight").toString().isEmpty()
                    ? Float.valueOf(requestData.get("OldWight").toString())
                    : 0.0f; // Default value for oldWeight

            Double oldRate = requestData.containsKey("OldRate") && requestData.get("OldRate") != null && !requestData.get("OldRate").toString().isEmpty()
                    ? Double.valueOf(requestData.get("OldRate").toString())
                    : 0.0; // Default value for oldRate


            List<InhouseInvoice> inhouseInvoices=inhouseInvoiceRepo.findByUserNameAndStatus(userName,status);
            Integer closingQty=productStockRepo.findOPStockByPId(productId);
            System.out.println("Closing qty:-"+closingQty +"and "+quantity);
            if(closingQty>=quantity)
            {
                // Create an Item object (assuming you have an Item entity)
                InhouseInvoice item = new InhouseInvoice();
                item.setProdId(productId);
                item.setProdQty(quantity);
                item.setProdRate(rate);
                item.setCategoryId(categoryId);
                item.setProdTotalVal(totalAmount);
                item.setUserName(userName);
                item.setUserAddress(userAddress);
                item.setSeriesNo(seriesNo);
                item.setOldRate(oldRate);
                item.setOldWight(oldWeight);


                Map<String, Object> response = new HashMap<>();
                response.put("message", "Item saved successfully!");
                response.put("inhouseInvoices", inhouseInvoices);
                // Save the item using the service layer
                inhouseInvoiceRepo.save(item);
                model.addAttribute("inhouseInvoices",inhouseInvoices);
                // Return success response
                companyInfo(model);
                System.out.println("response"+response);
                return ResponseEntity.ok(response);
            }
            else{

                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "You can't issue more then actual stock");
                companyInfo(model);
                System.out.println("errorResponse"+errorResponse);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }

        } catch (Exception e) {

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to save item.");
            companyInfo(model);
            System.out.println("catch"+errorResponse);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/save-item-black/")
    public ResponseEntity<Map<String,Object>> saveItemBlack(@RequestBody Map<String, Object> requestData,Model model) {
        try {
            String status=null;
            Long productId = Long.valueOf(requestData.get("productId").toString());
            Integer categoryId = Integer.valueOf(requestData.get("categoryId").toString());
            Integer quantity = Integer.valueOf(requestData.get("quantity").toString());
            Double rate = Double.valueOf(requestData.get("rate").toString());
            Double totalAmount = Double.valueOf(requestData.get("totalAmount").toString());
            String userName = requestData.get("userName").toString();
            String  userAddress = requestData.get("userAddress").toString();
            String  seriesNo = requestData.get("seriesNo").toString();

            List<InhouseInvoice> inhouseInvoices=inhouseInvoiceRepo.findByUserNameAndStatus(userName,status);
            System.out.println(inhouseInvoices);
            Integer closingQty=blackStockRepo.findOPStockByPId(productId);
            System.out.println("Closing qty:-"+closingQty +"and "+quantity);
            if(closingQty>=quantity)
            {
                // Create an Item object (assuming you have an Item entity)
                InhouseInvoice item = new InhouseInvoice();
                item.setProdId(productId);
                item.setProdQty(quantity);
                item.setProdRate(rate);
                item.setCategoryId(categoryId);
                item.setProdTotalVal(totalAmount);
                item.setUserName(userName);
                item.setUserAddress(userAddress);
                item.setSeriesNo(seriesNo);


                Map<String, Object> response = new HashMap<>();
                response.put("message", "Item saved successfully!");
                response.put("inhouseInvoices", inhouseInvoices);
                // Save the item using the service layer
                inhouseInvoiceRepo.save(item);
                model.addAttribute("inhouseInvoices",inhouseInvoices);
                // Return success response
                companyInfo(model);
                System.out.println("response"+response);
                return ResponseEntity.ok(response);
            }
            else{

                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "You can't issue more then actual stock");
                companyInfo(model);
                System.out.println("errorResponse"+errorResponse);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }

        } catch (Exception e) {

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to save item.");
            companyInfo(model);
            System.out.println("catch"+errorResponse);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @PostMapping("/fetch-details/")
    public ResponseEntity<?> fetchDetails(@RequestBody Map<String, Object> requestData) {
        try {

            String status=null;
            String userName = requestData.get("userName").toString();

//            List<InhouseInvoice> inhouseInvoicesList=inhouseInvoiceRepo.findByUserName(userName.toString());
            List<InhouseInvoice> inhouseInvoicesList=inhouseInvoiceRepo.findByUserNameAndStatus(userName.toString(),status);
            if(inhouseInvoicesList!=null)
            {
                return ResponseEntity.ok(inhouseInvoicesList);
            }
            else{
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Invoice is already made.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save item.");
        }
    }

    @PostMapping("/create-invoice/")
    public String getInvoice(@RequestBody InvoiceRequest requestData, RedirectAttributes redirectAttributes) {
        try {
            String userName = requestData.getUserName();
            String emailId = requestData.getEmailId();
            String mobNumber = requestData.getMobNumber();
            double gross = requestData.getGross();
            double gst = requestData.getGst();
            double discount = requestData.getDiscount();
            String paymentMode = requestData.getPaymentMode();
            String vehicleNumber = requestData.getVehicleNumber();

            String paymentStatus = requestData.getPaymentStatus();

            double cgst = gst / 2;
            double sgst = cgst;

            Double gstAmount=(gross*gst)/100;
            Double discountVal=((gross+gstAmount)*discount)/100;
            Double prizeithGst=gross+gstAmount;
            Double netAmtVal = prizeithGst-discountVal;
            double roundedNetAmt = Math.round(netAmtVal * 1000.0) / 1000.0;
            double paidAmount = (requestData.getPaidAmount() == 0) ? roundedNetAmt : requestData.getPaidAmount();
            // Fetch address for the user
            String status=null;
            List<InhouseInvoice> inhouseInvoicesList = inhouseInvoiceRepo.findByUserNameAndStatus(userName,status);

            Map<Long,Integer> stokDeatils=new HashMap<>();
            for (InhouseInvoice input:inhouseInvoicesList) {
                    Long prodId=input.getProdId();
                    Integer qty=input.getProdQty();
                    stokDeatils.put(prodId,stokDeatils.getOrDefault(prodId,0)+qty);
            }
            System.out.println("Stock deduction data:-"+stokDeatils);
            String address = inhouseInvoicesList.stream()
                    .map(InhouseInvoice::getUserAddress)
                    .findFirst()
                    .orElse("");

            // Create and populate the OrderPlaced object

            OrderPlaced orderPlaced = new OrderPlaced();
            orderPlaced.setUsername(userName);
            orderPlaced.setAddress(address);
            orderPlaced.setGrossAmount(gross);

            orderPlaced.setGst(gst);
            orderPlaced.setCgst(cgst);
            orderPlaced.setSgst(sgst);
            orderPlaced.setDiscount((int) discount);
            orderPlaced.setNetAmt(roundedNetAmt);
            orderPlaced.setPaymentMode(paymentMode);
            orderPlaced.setPaymentStatus(paymentStatus);
            orderPlaced.setPaidAmount(paidAmount);
            orderPlaced.setEmailId(emailId);
            orderPlaced.setMobNumber(mobNumber);
            orderPlaced.setVehicleNumber(vehicleNumber);

            // Save the orderPlaced entity
            orderPlacedRepo.save(orderPlaced);

            for (Map.Entry<Long, Integer> entry : stokDeatils.entrySet()) {
                Long key = entry.getKey();       // The key
                Integer value = entry.getValue(); // The value
                Integer closingStock= productStockRepo.findOPStockByPId(key);
                Integer newClosing= (closingStock-value);
                ProductStock productStock=new ProductStock();
                productStock.setOpeningStk(closingStock);
                productStock.setProductSelect(key);
                productStock.setIssueStk(value);
                productStock.setRecStock(0);
                productStock.setReturnStk(0);
                productStock.setClosingStk(newClosing);
                productStock.setUser(userName);
                productStock.setNote("Sell");
                productStock.setDoc("Inhouse Invoice");
                productStockRepo.save(productStock);

                stokDeatils.clear();

            }



            List<InhouseInvoice> inhouseInvoices=inhouseInvoiceRepo.findByUserName(userName);



            return "redirect:/admin/invoice/";
        } catch (Exception e) {

            return "errorPage";
        }
    }

    @PostMapping("/create-invoice-black/")
    public String getInvoiceBlack(@RequestBody InvoiceRequest requestData, RedirectAttributes redirectAttributes) {
        System.out.println("inside black invoice page");
        try {
            String userName = requestData.getUserName();
            String emailId = requestData.getEmailId();
            String mobNumber = requestData.getMobNumber();
            double gross = requestData.getGross();
            double gst = requestData.getGst();
            double discount = requestData.getDiscount();
            String paymentMode = requestData.getPaymentMode();
            String vehicleNumber = requestData.getVehicleNumber();

            String paymentStatus = requestData.getPaymentStatus();

            double cgst = gst / 2;
            double sgst = cgst;

            Double gstAmount=(gross*gst)/100;
            Double discountVal=((gross+gstAmount)*discount)/100;
            Double prizeithGst=gross+gstAmount;
            Double netAmtVal = prizeithGst-discountVal;
            double roundedNetAmt = Math.round(netAmtVal * 1000.0) / 1000.0;
            double paidAmount = (requestData.getPaidAmount() == 0) ? roundedNetAmt : requestData.getPaidAmount();
            // Fetch address for the user
            String status=null;
            List<InhouseInvoice> inhouseInvoicesList = inhouseInvoiceRepo.findByUserNameAndStatus(userName,status);

            Map<Long,Integer> stokDeatils=new HashMap<>();
            for (InhouseInvoice input:inhouseInvoicesList) {
                Long prodId=input.getProdId();
                Integer qty=input.getProdQty();
                stokDeatils.put(prodId,stokDeatils.getOrDefault(prodId,0)+qty);
            }
            System.out.println("Stock deduction data:-"+stokDeatils);
            String address = inhouseInvoicesList.stream()
                    .map(InhouseInvoice::getUserAddress)
                    .findFirst()
                    .orElse("");

            // Create and populate the OrderPlaced object

            OrderPlaced orderPlaced = new OrderPlaced();
            orderPlaced.setUsername(userName);
            orderPlaced.setAddress(address);
            orderPlaced.setGrossAmount(gross);

            orderPlaced.setGst(gst);
            orderPlaced.setCgst(cgst);
            orderPlaced.setSgst(sgst);
            orderPlaced.setDiscount((int) discount);
            orderPlaced.setNetAmt(roundedNetAmt);
            orderPlaced.setPaymentMode(paymentMode);
            orderPlaced.setPaymentStatus(paymentStatus);
            orderPlaced.setPaidAmount(paidAmount);
            orderPlaced.setEmailId(emailId);
            orderPlaced.setMobNumber(mobNumber);
            orderPlaced.setVehicleNumber(vehicleNumber);

            // Save the orderPlaced entity
            orderPlacedRepo.save(orderPlaced);

            for (Map.Entry<Long, Integer> entry : stokDeatils.entrySet()) {
                Long key = entry.getKey();       // The key
                Integer value = entry.getValue(); // The value
                Integer closingStock= blackStockRepo.findOPStockByPId(key);
                Integer newClosing= (closingStock-value);
                BlackStock productStock=new BlackStock();
                productStock.setOpeningStk(closingStock);
                productStock.setProductSelect(key);
                productStock.setIssueStk(value);
                productStock.setRecStock(0);
                productStock.setReturnStk(0);
                productStock.setClosingStk(newClosing);
                productStock.setUser(userName);
                System.out.println("Sell black"+orderPlaced.getInvoiceDate());
                productStock.setNote("Sell black"+orderPlaced.getInvoiceDate());
                productStock.setDoc("Inhouse Invoice");
                blackStockRepo.save(productStock);

                stokDeatils.clear();

            }



            List<InhouseInvoice> inhouseInvoices=inhouseInvoiceRepo.findByUserName(userName);



            return "redirect:/admin/invoice/black/";
        } catch (Exception e) {

            return "errorPage";
        }
    }


    @GetMapping("/admin/invoice/")
    public String getInvoice(@RequestParam(value = "userName", required = false) String userName, Model model) {
        if (userName == null || userName.isEmpty()) {
            companyInfo(model);
            model.addAttribute("error", "User name is missing.");
            return "errorPage";
        }
        List<OrderPlaced> orderPlacedList = orderPlacedRepo.findByusername(userName);

        if (orderPlacedList.isEmpty()) {
            companyInfo(model);
            model.addAttribute("errorMessage", "No records found for user: " + userName);
            return "errorPage";
        }
        else{
            // Initialize variables to store values from the last OrderPlaced
            Long id = null;
            Double grossAmount = null;
            Double cgst = null;
            Double sgst = null;
            Double gst = null;
            Integer discount = null;
            Double netAmt = null;
            Double paidAmount = null;
            String paymentMode = null;
            String paymentStatus = null;
            String address = null;
            String mobNumber=null;
            String emaiId=null;
            String vehicleNumber=null;

            // Loop through the list and get the last value
            for (OrderPlaced input : orderPlacedList) {
                id = input.getId();
                grossAmount = input.getGrossAmount();
                cgst = input.getCgst();
                sgst = input.getSgst();
                gst = input.getGst();
                discount = input.getDiscount();
                netAmt = input.getNetAmt();
                paidAmount = input.getPaidAmount();
                paymentMode = input.getPaymentMode();
                paymentStatus = input.getPaymentStatus();
                address = input.getAddress();
                emaiId = input.getEmailId();
                mobNumber = input.getMobNumber();
                vehicleNumber=input.getVehicleNumber();
            }
            String status=null;
            List<InhouseInvoice> inhouseInvoices=inhouseInvoiceRepo.findByUserNameAndStatus(userName,status);
            inhouseInvoices.forEach(inhouseInvoice -> inhouseInvoice.setStatus("Y"));
            Long finalId = id;
            inhouseInvoices.forEach(inhouseInvoice -> inhouseInvoice.setOrderId(finalId));
//            String invoiceDate = inhouseInvoices.get(0).getInvoiceDate().toString().formatted("yyyy-MM-dd HH:mm:ss");
            String invoiceDate = "";
            LocalDateTime invoiceDateTime =inhouseInvoices.get(0).getInvoiceDate();
            DateTimeFormatter dateTimeFormat=DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            invoiceDate = invoiceDateTime.format(dateTimeFormat);
            inhouseInvoiceRepo.saveAll(inhouseInvoices);

            List<Map<String,Object>> extractData=inhouseInvoices.stream()

                    .map(inhouseInvoice -> {
                Map<String,Object> data=new HashMap<>();
                String PName=productRepo.findPNameByPId(inhouseInvoice.getProdId());
                double unitPrize=productRepo.findPrizeByPId(inhouseInvoice.getProdId());
                data.put("id",inhouseInvoice.getId());
                data.put("Prod_Name",PName);
                data.put("unitPrize",unitPrize);
                data.put("qty",inhouseInvoice.getProdQty());
                        System.out.println("series_no"+inhouseInvoice.getSeriesNo());
                data.put("series_no",inhouseInvoice.getSeriesNo());
                data.put("amount",inhouseInvoice.getProdTotalVal());

                return data;
            }).collect(Collectors.toList());

            CompanyDetails details=new CompanyDetails();
            List<CompanyDetails> companyDetails=companyDetailsRepo.findAll();
            for (CompanyDetails comp:companyDetails) {
                model.addAttribute("Cname",comp.getcName());
                model.addAttribute("Cadd",comp.getcAddress());
                model.addAttribute("Cnumber",comp.getcNumber());
                model.addAttribute("gstNumber",comp.getGstNumber());
            }

            model.addAttribute("id", id);
            model.addAttribute("grossAmount", grossAmount);
            model.addAttribute("username", userName);
            model.addAttribute("cgst", cgst);
            model.addAttribute("sgst", sgst);
            model.addAttribute("gst", gst);
            model.addAttribute("discount", discount);
            model.addAttribute("netAmt", netAmt);
            model.addAttribute("paidAmount", paidAmount);
            model.addAttribute("paymentMode", paymentMode);
            model.addAttribute("paymentStatus", paymentStatus);
            model.addAttribute("mobNumber", mobNumber);
            model.addAttribute("emailId", emaiId);
            model.addAttribute("address", address);
            model.addAttribute("extractData", extractData);
            model.addAttribute("invoiceDate", invoiceDate);
            model.addAttribute("vehicle", vehicleNumber);
            companyInfo(model);
            return "Invoice"; // This will render the Invoice template
        }


    }

    @GetMapping("/admin/invoice/black/")
    public String getInvoiceBlack(@RequestParam(value = "userName", required = false) String userName, Model model) {
        if (userName == null || userName.isEmpty()) {
            companyInfo(model);
            model.addAttribute("error", "User name is missing.");
            return "errorPage";
        }
        List<OrderPlaced> orderPlacedList = orderPlacedRepo.findByusername(userName);

        if (orderPlacedList.isEmpty()) {
            companyInfo(model);
            model.addAttribute("errorMessage", "No records found for user: " + userName);
            return "errorPage";
        }
        else{
            // Initialize variables to store values from the last OrderPlaced
            Long id = null;
            Double grossAmount = null;
            Double cgst = null;
            Double sgst = null;
            Double gst = null;
            Integer discount = null;
            Double netAmt = null;
            Double paidAmount = null;
            String paymentMode = null;
            String paymentStatus = null;
            String address = null;
            String mobNumber=null;
            String emaiId=null;
            String vehicleNumber=null;

            // Loop through the list and get the last value
            for (OrderPlaced input : orderPlacedList) {
                id = input.getId();
                grossAmount = input.getGrossAmount();
                cgst = input.getCgst();
                sgst = input.getSgst();
                gst = input.getGst();
                discount = input.getDiscount();
                netAmt = input.getNetAmt();
                paidAmount = input.getPaidAmount();
                paymentMode = input.getPaymentMode();
                paymentStatus = input.getPaymentStatus();
                address = input.getAddress();
                emaiId = input.getEmailId();
                mobNumber = input.getMobNumber();
                vehicleNumber=input.getVehicleNumber();
            }
            String status=null;
            List<InhouseInvoice> inhouseInvoices=inhouseInvoiceRepo.findByUserNameAndStatus(userName,status);
            inhouseInvoices.forEach(inhouseInvoice -> inhouseInvoice.setStatus("Y"));
            Long finalId = id;
            inhouseInvoices.forEach(inhouseInvoice -> inhouseInvoice.setOrderId(finalId));
//            String invoiceDate = inhouseInvoices.get(0).getInvoiceDate().toString().formatted("yyyy-MM-dd HH:mm:ss");
            String invoiceDate = "";
            LocalDateTime invoiceDateTime =inhouseInvoices.get(0).getInvoiceDate();
            DateTimeFormatter dateTimeFormat=DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            invoiceDate = invoiceDateTime.format(dateTimeFormat);
            inhouseInvoiceRepo.saveAll(inhouseInvoices);

            List<Map<String,Object>> extractData = inhouseInvoices.stream()
                    .map(inhouseInvoice -> {
                        Map<String, Object> data = new HashMap<>();

                        // Get product details in an optimized way
                        Long prodId = inhouseInvoice.getProdId();
                        String PName = productRepo.findPNameByPId(prodId);
                        double unitPrize = productRepo.findPrizeByPId(prodId);

                        // Calculate GST and total values
                        double gstPrize = unitPrize * 0.20;  // 20% GST
                        double totalUnitPrice = unitPrize + gstPrize;
                        double amount = inhouseInvoice.getProdTotalVal() + gstPrize;

                        // Populate map with necessary values
                        data.put("id", inhouseInvoice.getId());
                        data.put("Prod_Name", PName);
                        data.put("series_no", inhouseInvoice.getSeriesNo());
                        data.put("unitPrize", totalUnitPrice);
                        data.put("qty", inhouseInvoice.getProdQty());
                        data.put("amount", amount);

                        // Return the data map
                        return data;
                    })
                    .collect(Collectors.toList());


            CompanyDetails details=new CompanyDetails();
            List<CompanyDetails> companyDetails=companyDetailsRepo.findAll();
            for (CompanyDetails comp:companyDetails) {
                model.addAttribute("Cname",comp.getcName());
                model.addAttribute("Cadd",comp.getcAddress());
                model.addAttribute("Cnumber",comp.getcNumber());
                model.addAttribute("gstNumber",comp.getGstNumber());
            }

            model.addAttribute("id", id);
            model.addAttribute("grossAmount", grossAmount);
            model.addAttribute("username", userName);
            model.addAttribute("cgst", cgst);
            model.addAttribute("sgst", sgst);
            model.addAttribute("gst", gst);
            model.addAttribute("discount", discount);
            model.addAttribute("netAmt", netAmt);
            model.addAttribute("paidAmount", paidAmount);
            model.addAttribute("paymentMode", paymentMode);
            model.addAttribute("paymentStatus", paymentStatus);
            model.addAttribute("mobNumber", mobNumber);
            model.addAttribute("emailId", emaiId);
            model.addAttribute("address", address);
            model.addAttribute("extractData", extractData);
            model.addAttribute("invoiceDate", invoiceDate);
            model.addAttribute("vehicle", vehicleNumber);
            companyInfo(model);
            return "BlackInvoice"; // This will render the Invoice template
        }


    }

    @PostMapping("/admin/product-stock/update/")
    public String updateStock(Model model, ProductStock productStock) {
        companyInfo(model);
        Logger logger = LoggerFactory.getLogger(this.getClass());
        List<Category> categoryList=categoryRepo.findAll();
        try {
            // Validate productStock input
            if (productStock == null || productStock.getProductSelect() == null) {
                logger.error("Invalid ProductStock data: {}", productStock);
                return "errorPage";
            }

            // Log input data
            logger.info("Updating stock for product: {}", productStock.getProductSelect());

            // Fetch previous stock (default to 0 if not found)
            Integer previousStk = productStockRepo.findOPStockByPId(productStock.getProductSelect());
            if (previousStk == null) {
                logger.warn("Previous stock not found for product ID: {}. Defaulting to 0.", productStock.getProductSelect());
                previousStk = 0;
            }

            // Handle potential null values for fields
            Integer openingStk = (productStock.getOpeningStk() != null) ? previousStk : 0;
            Integer issueStk = (productStock.getIssueStk() != null) ? productStock.getIssueStk() : 0;
            Integer returnStk = (productStock.getReturnStk() != null) ? productStock.getReturnStk() : 0;
            Integer recStock = (productStock.getRecStock() != null) ? previousStk+productStock.getRecStock() : 0;

            // Calculate new stock values
            Integer newStock = recStock + previousStk;
            Integer closingStk = newStock - issueStk + returnStk;

            // Create new ProductStock entry
            ProductStock productStock1 = new ProductStock();
            productStock1.setProductSelect(productStock.getProductSelect());
            productStock1.setOpeningStk(previousStk);
            productStock1.setRecStock(productStock.getRecStock());
            productStock1.setIssueStk(0); // Resetting issue stock
            productStock1.setReturnStk(0); // Resetting return stock
            productStock1.setClosingStk(closingStk);
            productStock1.setUser("Admin");
            productStock1.setDoc("Stock Add");
            productStock1.setNote(productStock.getNote());

            // Save to database
            productStockRepo.save(productStock1);

            model.addAttribute("msg","Product Stock updated Successfully");
            model.addAttribute("categoryList",categoryList);
            return "ProductStock";

        } catch (DataAccessException dae) {
            logger.error("Database error occurred: {}", dae.getMessage(), dae);
            return "errorPage";
        } catch (Exception e) {
            logger.error("Unexpected error occurred: {}", e.getMessage(), e);
            return "errorPage";
        }
    }

    @PostMapping("/admin/black-product-stock/update/")
    public String updateBlackStock(Model model, BlackStock productStock) {

        companyInfo(model);
        Logger logger = LoggerFactory.getLogger(this.getClass());
        List<Category> categoryList=categoryRepo.findAll();
        try {
            // Validate productStock input
            if (productStock == null || productStock.getProductSelect() == null) {
                logger.error("Invalid ProductStock data: {}", productStock);
                return "errorPage";
            }

            // Log input data
            logger.info("Updating stock for product: {}", productStock.getProductSelect());

            // Fetch previous stock (default to 0 if not found)
            Integer previousStk = blackStockRepo.findOPStockByPId(productStock.getProductSelect());
            if (previousStk == null) {
                logger.warn("Previous stock not found for product ID: {}. Defaulting to 0.", productStock.getProductSelect());
                previousStk = 0;
            }

            // Handle potential null values for fields
            Integer openingStk = (productStock.getOpeningStk() != null) ? previousStk : 0;
            Integer issueStk = (productStock.getIssueStk() != null) ? productStock.getIssueStk() : 0;
            Integer returnStk = (productStock.getReturnStk() != null) ? productStock.getReturnStk() : 0;
            Integer recStock = (productStock.getRecStock() != null) ? previousStk+productStock.getRecStock() : 0;

            // Calculate new stock values
            Integer newStock = recStock + previousStk;
            Integer closingStk = newStock - issueStk + returnStk;

            // Create new ProductStock entry
            BlackStock productStock1 = new BlackStock();
            productStock1.setProductSelect(productStock.getProductSelect());
            productStock1.setOpeningStk(previousStk);
            productStock1.setRecStock(productStock.getRecStock());
            productStock1.setIssueStk(0); // Resetting issue stock
            productStock1.setReturnStk(0); // Resetting return stock
            productStock1.setClosingStk(closingStk);
            productStock1.setUser("Admin");
            productStock1.setDoc("Stock Add");
            productStock1.setNote(productStock.getNote());

            // Save to database
            blackStockRepo.save(productStock1);

            model.addAttribute("msg","Product Stock updated Successfully");
            model.addAttribute("categoryList",categoryList);
            return "ProductStock";

        } catch (DataAccessException dae) {
            logger.error("Database error occurred: {}", dae.getMessage(), dae);
            return "errorPage";
        } catch (Exception e) {
            logger.error("Unexpected error occurred: {}", e.getMessage(), e);
            return "errorPage";
        }
    }

    @GetMapping("/company/")
    public String getCompanyPage(Model model)
    {
        companyInfo(model);
        return "CompanyDetails";
    }

    @PostMapping("/admin/save/company/details/")
    public String getData(Model model,@RequestParam("cName") String cName,
                          @RequestParam("cAddress") String address,
                          @RequestParam("cNumber") String mobNumber,
                          @RequestParam("gstnYn") String yn,
                          @RequestParam("gstNumber") String gstNumber,
                          @RequestParam("logo") MultipartFile logo)
    {
        try
        {
            CompanyDetails companyDetails=new CompanyDetails();
            companyDetails.setcName(cName);
            companyDetails.setcAddress(address);
            companyDetails.setcNumber(mobNumber);
            companyDetails.setGstnYn(yn);
            companyDetails.setGstNumber(gstNumber);
            companyDetails.setLogo(logo.getBytes());
            companyDetailsRepo.save(companyDetails);
            companyInfo(model);
            model.addAttribute("msg","Data saved successfully");
            return "CompanyDetails";
        }catch (Exception e)
        {
            return "errorPage";
        }

    }

    @GetMapping("/admin/view/invoice/")
    public String viewInvoice(@RequestParam(value = "orderId", required = false) Long orderId, Model model) {
        if (orderId == null) {
            model.addAttribute("error", "Order ID is missing.");
            return "errorPage";
        }

        Optional<OrderPlaced> optionalOrder = orderPlacedRepo.findById(orderId);

        if (optionalOrder==null) {
            model.addAttribute("errorMessage", "No records found for order: " + orderId);
            return "errorPage";
        }

        OrderPlaced input = optionalOrder.get();

        // Extract order details
        Long id = input.getId();
        Double grossAmount = input.getGrossAmount();
        Double cgst = input.getCgst();
        Double sgst = input.getSgst();
        Double gst = input.getGst();
        Integer discount = input.getDiscount();
        Double netAmt = input.getNetAmt();
        Double paidAmount = input.getPaidAmount();
        String paymentMode = input.getPaymentMode();
        String paymentStatus = input.getPaymentStatus();
        String address = input.getAddress();
        String emailId = input.getEmailId();
        String mobNumber = input.getMobNumber();
        String vehicleNumber = input.getVehicleNumber();

        // Retrieve invoices
        List<InhouseInvoice> inhouseInvoices = inhouseInvoiceRepo.findByOrderId(orderId);

        if (inhouseInvoices.isEmpty()) {
            model.addAttribute("error", "No invoices found for order: " + orderId);
            return "errorPage";
        }

        // Set status and order ID
        inhouseInvoices.forEach(inhouseInvoice -> {
            inhouseInvoice.setStatus("Y");
            inhouseInvoice.setOrderId(id);
        });
        inhouseInvoiceRepo.saveAll(inhouseInvoices);

        // Extract the invoice date from the first invoice (assuming all invoices share the same date)
        String invoiceDate = "";
        LocalDateTime invoiceDateTime =inhouseInvoices.get(0).getInvoiceDate();
        DateTimeFormatter dateTimeFormat=DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        invoiceDate = invoiceDateTime.format(dateTimeFormat);
        inhouseInvoiceRepo.saveAll(inhouseInvoices);

        List<Map<String, Object>> extractData = inhouseInvoices.stream()
                .map(inhouseInvoice -> {
                    Map<String, Object> data = new HashMap<>();
                    String PName = productRepo.findPNameByPId(inhouseInvoice.getProdId());
                    double unitPrize = productRepo.findPrizeByPId(inhouseInvoice.getProdId());
                    data.put("id", inhouseInvoice.getId());
                    data.put("Prod_Name", PName);
                    data.put("unitPrize", unitPrize);
                    data.put("qty", inhouseInvoice.getProdQty());
                    data.put("amount", inhouseInvoice.getProdTotalVal());
                    data.put("series_no", inhouseInvoice.getSeriesNo());
                    return data;
                })
                .collect(Collectors.toList());

        // Add company details
        List<CompanyDetails> companyDetails = companyDetailsRepo.findAll();
        if (!companyDetails.isEmpty()) {
            CompanyDetails comp = companyDetails.get(0);
            model.addAttribute("Cname", comp.getcName());
            model.addAttribute("Cadd", comp.getcAddress());
            model.addAttribute("Cnumber", comp.getcNumber());
            model.addAttribute("gstNumber", comp.getGstNumber());
        }

        model.addAttribute("id", id);
        model.addAttribute("grossAmount", grossAmount);
        model.addAttribute("cgst", cgst);
        model.addAttribute("sgst", sgst);
        model.addAttribute("gst", gst);
        model.addAttribute("discount", discount);
        model.addAttribute("netAmt", netAmt);
        model.addAttribute("paidAmount", paidAmount);
        model.addAttribute("paymentMode", paymentMode);
        model.addAttribute("paymentStatus", paymentStatus);
        model.addAttribute("mobNumber", mobNumber);
        model.addAttribute("emailId", emailId);
        model.addAttribute("address", address);
        model.addAttribute("extractData", extractData);
        model.addAttribute("invoiceDate", invoiceDate);
        model.addAttribute("vehicle", vehicleNumber);
        companyInfo(model);
        return "Invoice"; // Render the Invoice page
    }

    @GetMapping("/Product/stock/report/")
    public String getProductStock(Model model,
                                  @RequestParam(defaultValue = "1") int curPage,
                                  @RequestParam(defaultValue = "8") int size) {
        companyInfo(model);
        // Ensure the current page is at least 1
        curPage = Math.max(curPage, 1);

        // Fetch the full list of product stocks ordered by `product_select`
        List<ProductStock> allStocks = productStockRepo.orderByproduct_select();

        // Calculate total records and total pages
        long totalRecords = allStocks.size();
        int totalPages = (int) Math.ceil((double) totalRecords / size);

        // Adjust curPage if it exceeds totalPages
        curPage = Math.min(curPage, totalPages);

        // Calculate the start and end indexes for sub-listing
        int startIndex = (curPage - 1) * size;
        int endIndex = Math.min(startIndex + size, (int) totalRecords);

        // Create the paginated sublist
        List<ProductStock> paginatedStocks = allStocks.subList(startIndex, endIndex);

        // Fetch product names for the given product IDs
        Map<Long, String> productNamesMap = new HashMap<>();
        for (ProductStock stock : paginatedStocks) {
            String productName = productRepo.findPNameByPId(stock.getProductSelect());
            productNamesMap.put(stock.getProductSelect(), productName);
        }

        // Add attributes to the model
        model.addAttribute("curPage", curPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("productStockList", paginatedStocks);
        model.addAttribute("productNamesMap", productNamesMap);
        model.addAttribute("recordSize", totalRecords);

        return "ProductStockReport"; // Return the view
    }

    @GetMapping("/admin/get/invoice/items/")
    @ResponseBody
    public List<InhouseInvoice> getInvoiceItems(@RequestParam("userName") String userName,Model model) {
        companyInfo(model);
        String status = null;
        return inhouseInvoiceRepo.findByUserNameAndStatus(userName, status);
    }

    @DeleteMapping("/admin/delete/invoice/item/{id}")
    public ResponseEntity<String> deleteInvoiceItem(@PathVariable Long id,Model model) {
        try {
            companyInfo(model);
            // Check if the item exists
            if (!inhouseInvoiceRepo.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Invoice item not found");
            }

            // Delete the item
            inhouseInvoiceRepo.deleteById(id);
            return ResponseEntity.ok("Invoice item deleted successfully");
        } catch (Exception e) {
            // Handle unexpected exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while deleting the item");
        }
    }

    @GetMapping("/admin/product/black/stock/")
    public String getBlackProductStockPage(Model model)
    {
        List<Category> categoryList=categoryRepo.findAll();
        model.addAttribute("categoryList",categoryList);
        companyInfo(model);
        return "BlackProductStock";
    }

    @GetMapping("/admin/company/report/")
    public String getCompanyDetails(Model model) {
        List<CompanyDetails> companyDetailsList=companyDetailsRepo.findAll();
        model.addAttribute("company_Details", companyDetailsList);
        return "CompanyDetails";
    }

    @GetMapping("/admin/Incomplete/report/")
    public String getIncompleteInvoice(Model model) {
        List<OrderPlaced> orderPlacedList=orderPlacedRepo.findByPaidAmountLess();
        model.addAttribute("orderPlacedList", orderPlacedList);
        return "incompleteInhouceInvoice";
    }
    


    @GetMapping("/admin/old-battery/report/")
    public String getOldBatteryReport(
            @RequestParam(defaultValue = "1") int curPage,
            @RequestParam(defaultValue = "10") int maxSize,
            Model model) {

        // Pageable setup with dynamic page number and size
        Pageable pageable = PageRequest.of(curPage - 1, maxSize, Sort.by("id").descending());

        // Fetch paginated invoices
        Page<InhouseInvoice> page = inhouseInvoiceRepo.findByOldWeight(pageable);

        // Extract paginated content
        List<InhouseInvoice> paginatedInvoices = page.getContent();
        int totalPages = page.getTotalPages();

        // Add attributes to the model for the view
        model.addAttribute("curPage", curPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("invoiceList", paginatedInvoices);
        model.addAttribute("recordSize", page.getTotalElements());

        // Add company info to the model
        companyInfo(model);

        // Return the view name
        return "oldBatteryReport";
    }

    @GetMapping("/admin/companyDetails/report/")
    public String getCompanyDetailsReport(Model model) {

        List<CompanyDetails> companyDetails=companyDetailsRepo.findAll();
        model.addAttribute("details",companyDetails);
        return "companyDetailsReport";
    }

    @GetMapping("/admin/edit/company/{id}/")
    public String editCompanyDetails(Model model,@PathVariable("id") Long id)
    {
        CompanyDetails companyDetails=companyDetailsRepo.getReferenceById(id);
        model.addAttribute("data",companyDetails);
        List<CompanyDetails> companyDetail=companyDetailsRepo.findAll();
        model.addAttribute("details",companyDetail);
        return "CompanyDetails";
    }

    @GetMapping("/admin/delete/company/{id}/")
    public String deleteCompanyDetails(Model model,@PathVariable("id") Long id)
    {
        companyDetailsRepo.deleteById(id);
        List<CompanyDetails> companyDetail=companyDetailsRepo.findAll();
        model.addAttribute("details",companyDetail);
        return "companyDetailsReport";
    }

    @PostMapping("/admin/update/paid/amount/")
    public String updatePaidAmount(@RequestParam("invoiceId") String invoiceId,
                                   @RequestParam("paidAmount") String paidAmount,Model model) {
        try{
            String paymentStatus="paid";
            Long invoiceIdLong=Long.parseLong(invoiceId);
            OrderPlaced orderPlacedList=orderPlacedRepo.getReferenceById(invoiceIdLong);
            System.out.println(Double.parseDouble(paidAmount)==orderPlacedList.getNetAmt());
            if(Double.parseDouble(paidAmount)+orderPlacedList.getPaidAmount()==orderPlacedList.getNetAmt())
            {
                paymentStatus="paid";
            }
            else{
                paymentStatus="half_payment";
            }
            OrderPlaced updatedData=new OrderPlaced(orderPlacedList.getId(), orderPlacedList.getGrossAmount(), orderPlacedList.getCgst(),orderPlacedList.getSgst(),orderPlacedList.getGst(), orderPlacedList.getDiscount(),orderPlacedList.getNetAmt(),Double.parseDouble(paidAmount)+orderPlacedList.getPaidAmount(), orderPlacedList.getPaymentMode(), paymentStatus, orderPlacedList.getMobNumber(), orderPlacedList.getUsername(), orderPlacedList.getAddress(), orderPlacedList.getEmailId(), orderPlacedList.getVehicleNumber(), orderPlacedList.getInvoiceDate());
            orderPlacedRepo.save(updatedData);
            model.addAttribute("msg","Record Updated Successfully!");
            return "redirect:/admin/inhouseInvoice/report/";

        }catch(Exception e)
        {
            return "errorPage";
        }


    }














}
