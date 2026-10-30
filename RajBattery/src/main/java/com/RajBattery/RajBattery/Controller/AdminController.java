package com.RajBattery.RajBattery.Controller;

import com.RajBattery.RajBattery.Entity.*;
import com.RajBattery.RajBattery.Repo.*;
import com.RajBattery.RajBattery.Service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired private CompanyService companyService;
    @Autowired private ProductService productService;
    @Autowired private InvoiceService invoiceService;
    @Autowired private StockService stockService;
    @Autowired private UserService userService;
    @Autowired private OrderPlacedService orderPlacedService;
    @Autowired private PurchaseOrderService purchaseOrderService;

    @Autowired private CategoryRepo categoryRepo;
    @Autowired private InhouseInvoiceRepo inhouseInvoiceRepo;
    @Autowired private OrderPlacedRepo orderPlacedRepo;

    // ── DASHBOARD ─────────────────────────────────────────────────────────────

    @GetMapping("/admin/dashboard/")
    public String getHome(Model model) {
        companyService.populateCompanyInfo(model);
        model.addAttribute("totalProduct", productService.getAll().size());
        model.addAttribute("onlineCustomer", userService.getOnlineCustomerCount());
        Map<String, Double> agg = orderPlacedService.getAggregateAmounts();
        model.addAttribute("totalNetAmt", String.format("%.2f", agg.getOrDefault("totalNetAmt", 0.0)));
        model.addAttribute("remainingAmt", String.format("%.2f", agg.getOrDefault("totalDueAmt", 0.0)));
        Map<String, Long> poCounts = purchaseOrderService.getStatusCounts();
        long totalPO = poCounts.values().stream().mapToLong(Long::longValue).sum();
        long pendingPO = poCounts.getOrDefault("DRAFT", 0L) + poCounts.getOrDefault("SENT", 0L);
        model.addAttribute("totalPO", totalPO);
        model.addAttribute("pendingPO", pendingPO);
        return "adminDashboard";
    }

    // ── PRODUCT ───────────────────────────────────────────────────────────────

    @GetMapping("/new/Product/")
    public String getNewProduct(Model model) {
        companyService.populateCompanyInfo(model);
        model.addAttribute("categoryList", categoryRepo.findAll());
        return "ProductForm";
    }

    @PostMapping("/admin/addnewproduct/")
    public String handleProductData(Model model, Product product, MultipartFile file) {
        companyService.populateCompanyInfo(model);
        model.addAttribute("categoryList", categoryRepo.findAll());
        try {
            if (product.getPId() == null) {
                if (productService.existsByName(product.getPName())) {
                    model.addAttribute("emsg", "Product already exists.");
                    return "ProductForm";
                }
                productService.saveWithFile(product, file);
                model.addAttribute("msg", "Product registration successfully completed.");
            } else {
                productService.saveWithFile(product, file);
                model.addAttribute("msg", "Product updated successfully.");
            }
        } catch (Exception e) {
            logger.error("Product save error: {}", e.getMessage());
            model.addAttribute("emsg", "An error occurred while processing the request.");
        }
        return "ProductForm";
    }

    @GetMapping("/Product/report/")
    public String productReportDefault() {
        return "redirect:/Product/report/0";
    }

    @GetMapping("/Product/report/{pageNo}")
    public String getProductReport(@PathVariable int pageNo, Model model) {
        companyService.populateCompanyInfo(model);
        Pageable pageable = PageRequest.of(pageNo, 10, Sort.by("PId").descending());
        Page<Product> page = productService.getPaginated(pageable);
        model.addAttribute("curPage", pageNo);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("productList", page.getContent());
        model.addAttribute("recordSize", page.getTotalElements());
        return "ProductReport";
    }

    @GetMapping("/admin/delete/product/{PId}/")
    public String deleteProductRecord(@PathVariable Long PId, Model model) {
        productService.deleteById(PId);
        companyService.populateCompanyInfo(model);
        List<Product> all = productService.getAll();
        model.addAttribute("productList", all);
        model.addAttribute("recordSize", all.size());
        model.addAttribute("msg", "Record Deleted");
        return "ProductReport";
    }

    @GetMapping("/admin/edit/product/{PId}/")
    public String updateProductDetails(@PathVariable Long PId, Model model) {
        companyService.populateCompanyInfo(model);
        model.addAttribute("data", productService.getById(PId));
        model.addAttribute("categoryList", categoryRepo.findAll());
        return "ProductForm";
    }

    @GetMapping("/api/products/search")
    @ResponseBody
    public List<Map<String, Object>> searchProducts(@RequestParam String query) {
        return productService.searchByName(query);
    }

    @GetMapping("/api/products/{id}")
    @ResponseBody
    public Map<String, Object> getProductById(@PathVariable Long id) {
        return productService.getProductMapById(id);
    }

    // ── CATEGORY ──────────────────────────────────────────────────────────────

    @GetMapping("/admin/create/category/")
    public String setCategory(Model model) {
        companyService.populateCompanyInfo(model);
        return "ProductCategory";
    }

    @PostMapping("/admin/saved/category/")
    public String savedCategory(Category category, Model model) {
        companyService.populateCompanyInfo(model);
        if (categoryRepo.findByCate(category.getCate()).isEmpty()) {
            categoryRepo.save(category);
            model.addAttribute("msg", "Category saved successfully");
        } else {
            model.addAttribute("emsg", "Category already exists");
        }
        return "ProductCategory";
    }

    // ── STOCK ─────────────────────────────────────────────────────────────────

    @GetMapping("/admin/product/stock/")
    public String setProductStock(Model model) {
        companyService.populateCompanyInfo(model);
        model.addAttribute("categoryList", categoryRepo.findAll());
        return "ProductStock";
    }

    @PostMapping("/admin/product-stock/update/")
    public String updateStock(ProductStock productStock, Model model) {
        companyService.populateCompanyInfo(model);
        model.addAttribute("categoryList", categoryRepo.findAll());
        try {
            if (productStock == null || productStock.getProductSelect() == null) return "errorPage";
            Integer rec = (productStock.getQty() != null) ? productStock.getQty() : 0;
            if (rec <= 0) {
                model.addAttribute("emsg", "Quantity must be greater than 0");
                return "ProductStock";
            }
            stockService.receiveStock(productStock.getProductSelect(), rec, "Stock Add", productStock.getNote(), "Admin");
            model.addAttribute("msg", "Product Stock updated Successfully");
            return "ProductStock";
        } catch (DataAccessException e) {
            logger.error("DB error updating stock: {}", e.getMessage());
            return "errorPage";
        } catch (Exception e) {
            logger.error("Error updating stock: {}", e.getMessage());
            return "errorPage";
        }
    }

    @GetMapping("/Product/stock/report/")
    public String getProductStock(Model model,
                                  @RequestParam(defaultValue = "1") int curPage,
                                  @RequestParam(defaultValue = "8") int size) {
        companyService.populateCompanyInfo(model);
        curPage = Math.max(curPage, 1);
        List<ProductStock> all = stockService.getAllStocksOrdered();
        int totalPages = (int) Math.ceil((double) all.size() / size);
        curPage = Math.min(curPage, Math.max(totalPages, 1));
        int start = (curPage - 1) * size;
        int end = Math.min(start + size, all.size());
        List<ProductStock> paginated = all.subList(start, end);

        Map<Long, String> namesMap = new HashMap<>();
        for (ProductStock s : paginated) {
            namesMap.put(s.getProductSelect(),
                    productService.findById(s.getProductSelect()).map(Product::getPName).orElse(""));
        }
        model.addAttribute("curPage", curPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("productStockList", paginated);
        model.addAttribute("productNamesMap", namesMap);
        model.addAttribute("recordSize", all.size());
        return "ProductStockReport";
    }

    // ── INVOICE ITEMS ─────────────────────────────────────────────────────────

    @GetMapping("/admin/inhouse/invoice/")
    public String setInhouseInvoice(Model model) {
        companyService.populateCompanyInfo(model);
        model.addAttribute("categoryList", categoryRepo.findAll());
        return "InhouseInvoice";
    }

    @PostMapping("/save-item/")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveItem(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long productId = Long.valueOf(requestData.get("productId").toString());
            Integer quantity = Integer.valueOf(requestData.get("quantity").toString());
            Double rate = Double.valueOf(requestData.get("rate").toString());
            Double totalAmount = Double.valueOf(requestData.get("totalAmount").toString());
            String userName = requestData.get("userName").toString();

            if (quantity <= 0 || rate <= 0 || totalAmount <= 0) {
                response.put("message", "Rate and Quantity must be greater than 0");
                return ResponseEntity.badRequest().body(response);
            }

            int availableStock = stockService.getCurrentStock(productId);
            if (availableStock < quantity) {
                response.put("message", "Insufficient stock. Available: " + availableStock);
                return ResponseEntity.badRequest().body(response);
            }

            InhouseInvoice item = new InhouseInvoice();
            item.setProdId(productId);
            item.setCategoryId(Integer.valueOf(requestData.get("categoryId").toString()));
            item.setProdQty(quantity);
            item.setProdRate(rate);
            item.setProdTotalVal(totalAmount);
            item.setUserName(userName);
            item.setUserAddress(requestData.get("userAddress").toString());
            item.setSeriesNo(requestData.get("seriesNo").toString());
            item.setOldRate(parseDoubleOrDefault(requestData.get("OldRate")));
            item.setOldWight(parseFloatOrDefault(requestData.get("OldWight")));

            invoiceService.saveItem(item);
            response.put("message", "Item saved successfully");
            response.put("inhouseInvoices", invoiceService.getPendingInvoices(userName));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("saveItem error: {}", e.getMessage());
            response.put("message", "Server error while saving item");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/fetch-details/")
    @ResponseBody
    public ResponseEntity<?> fetchDetails(@RequestBody Map<String, Object> requestData) {
        try {
            String userName = requestData.get("userName").toString();
            return ResponseEntity.ok(invoiceService.getPendingInvoices(userName));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch details.");
        }
    }

    @GetMapping("/admin/get/invoice/items/")
    @ResponseBody
    public List<InhouseInvoice> getInvoiceItems(@RequestParam("userName") String userName) {
        return invoiceService.getPendingInvoices(userName);
    }

    @DeleteMapping("/admin/delete/invoice/item/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteInvoiceItem(@PathVariable Long id) {
        if (!invoiceService.itemExists(id))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invoice item not found");
        invoiceService.deleteItem(id);
        return ResponseEntity.ok("Invoice item deleted successfully");
    }

    // ── CREATE INVOICE ────────────────────────────────────────────────────────

    @PostMapping("/create-invoice/")
    public String createInvoice(@RequestBody InvoiceRequest req, RedirectAttributes ra) {
        try {
            return invoiceService.createInvoice(req);
        } catch (IllegalStateException e) {
            logger.error("createInvoice error: {}", e.getMessage());
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/inhouse/invoice/";
        } catch (Exception e) {
            logger.error("createInvoice error: {}", e.getMessage());
            return "errorPage";
        }
    }

    // ── VIEW INVOICE ──────────────────────────────────────────────────────────

    @GetMapping("/admin/invoice/")
    public String getInvoice(@RequestParam(value = "orderId", required = false) Long orderId, Model model) {
        if (orderId == null) {
            model.addAttribute("error", "Order ID is missing.");
            return "errorPage";
        }
        Map<String, Object> data = invoiceService.prepareInvoiceModelByOrderId(orderId);
        if (data == null) {
            model.addAttribute("errorMessage", "No records found.");
            return "errorPage";
        }
        data.forEach(model::addAttribute);
        companyService.populateCompanyInfo(model);
        return "Invoice";
    }

    @GetMapping("/admin/view/invoice/")
    public String viewInvoice(@RequestParam(value = "orderId", required = false) Long orderId, Model model) {
        if (orderId == null) {
            model.addAttribute("error", "Order ID is missing.");
            return "errorPage";
        }
        Map<String, Object> data = invoiceService.prepareInvoiceModelByOrderId(orderId);
        if (data == null) {
            model.addAttribute("errorMessage", "No records found.");
            return "errorPage";
        }
        data.forEach(model::addAttribute);
        companyService.populateCompanyInfo(model);
        return "Invoice";
    }

    // ── SELL REPORT ───────────────────────────────────────────────────────────

    @GetMapping("/admin/inhouseInvoice/report/")
    public String getInhouseInvoiceReport(@RequestParam(defaultValue = "1") int curPage,
                                          @RequestParam(defaultValue = "8") int size,
                                          Model model) {
        return loadInhouseReport(model, curPage, size);
    }

    @GetMapping("/admin/delete/invoice/{id}/")
    public String deleteInvoice(@PathVariable Long id,
                                @RequestParam(defaultValue = "1") int curPage,
                                @RequestParam(defaultValue = "8") int size,
                                Model model) {
        orderPlacedRepo.deleteById(id);
        return loadInhouseReport(model, curPage, size);
    }

    private String loadInhouseReport(Model model, int curPage, int size) {
        companyService.populateCompanyInfo(model);
        curPage = Math.max(curPage, 1);
        Pageable pageable = PageRequest.of(curPage - 1, size, Sort.by("id").descending());
        Page<OrderPlaced> page = orderPlacedRepo.findAll(pageable);
        if (page.getTotalPages() > 0 && curPage > page.getTotalPages()) {
            curPage = page.getTotalPages();
            page = orderPlacedRepo.findAll(PageRequest.of(curPage - 1, size, Sort.by("id").descending()));
        }
        Map<String, Double> agg = orderPlacedService.getAggregateAmounts();
        model.addAttribute("curPage", curPage);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("orderPlacedList", page.getContent());
        model.addAttribute("recordSize", page.getTotalElements());
        model.addAttribute("totalNetAmt", agg.get("totalNetAmt"));
        model.addAttribute("totalPaidAmt", agg.get("totalPaidAmt"));
        model.addAttribute("totalDueAmt", agg.get("totalDueAmt"));
        return "InhouseSellReport";
    }

    @GetMapping("/admin/Incomplete/report/")
    public String getIncompleteInvoice(Model model) {
        companyService.populateCompanyInfo(model);
        model.addAttribute("orderPlacedList", orderPlacedRepo.findByPaidAmountLess());
        return "incompleteInhouceInvoice";
    }

    @GetMapping("/admin/old-battery/report/")
    public String getOldBatteryReport(@RequestParam(defaultValue = "1") int curPage,
                                      @RequestParam(defaultValue = "10") int maxSize,
                                      Model model) {
        companyService.populateCompanyInfo(model);
        Pageable pageable = PageRequest.of(curPage - 1, maxSize, Sort.by("id").descending());
        Page<InhouseInvoice> page = inhouseInvoiceRepo.findByOldWeight(pageable);
        model.addAttribute("curPage", curPage);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("invoiceList", page.getContent());
        model.addAttribute("recordSize", page.getTotalElements());
        return "oldBatteryReport";
    }

    @GetMapping("/admin/onlineSell/report/")
    public String getOnlineInvoiceReport(Model model) {
        companyService.populateCompanyInfo(model);
        return "OnlineSellingReport";
    }

    @PostMapping("/admin/update/paid/amount/")
    public String updatePaidAmount(@RequestParam("invoiceId") String invoiceId,
                                   @RequestParam("paidAmount") String paidAmount,
                                   Model model) {
        try {
            Long id = Long.parseLong(invoiceId);
            OrderPlaced op = orderPlacedRepo.getReferenceById(id);
            double newPaid = Double.parseDouble(paidAmount) + op.getPaidAmount();
            String status = (newPaid >= op.getNetAmt()) ? "paid" : "half_payment";
            OrderPlaced updated = new OrderPlaced(
                    op.getId(), op.getGrossAmount(), op.getCgst(), op.getSgst(),
                    op.getGst(), op.getDiscount(), op.getNetAmt(), newPaid,
                    op.getPaymentMode(), status, op.getMobNumber(), op.getUsername(),
                    op.getAddress(), op.getEmailId(), op.getVehicleNumber(), op.getInvoiceDate());
            orderPlacedRepo.save(updated);
            return "redirect:/admin/inhouseInvoice/report/";
        } catch (Exception e) {
            logger.error("updatePaidAmount error: {}", e.getMessage());
            return "errorPage";
        }
    }

    // ── COMPANY DETAILS ───────────────────────────────────────────────────────

    @GetMapping("/company/")
    public String getCompanyPage(Model model) {
        companyService.populateCompanyInfo(model);
        return "CompanyDetails";
    }

    @PostMapping("/admin/save/company/details/")
    public String saveCompanyDetails(@RequestParam("cName") String cName,
                                     @RequestParam("cAddress") String address,
                                     @RequestParam("cNumber") String mobNumber,
                                     @RequestParam("gstnYn") String yn,
                                     @RequestParam("gstNumber") String gstNumber,
                                     @RequestParam("logo") MultipartFile logo,
                                     Model model) {
        try {
            CompanyDetails cd = new CompanyDetails();
            cd.setcName(cName);
            cd.setcAddress(address);
            cd.setcNumber(mobNumber);
            cd.setGstnYn(yn);
            cd.setGstNumber(gstNumber);
            cd.setLogo(logo.getBytes());
            companyService.save(cd);
            companyService.populateCompanyInfo(model);
            model.addAttribute("msg", "Data saved successfully");
            return "CompanyDetails";
        } catch (Exception e) {
            logger.error("saveCompanyDetails error: {}", e.getMessage());
            return "errorPage";
        }
    }

    @GetMapping("/admin/company/report/")
    public String getCompanyDetails(Model model) {
        companyService.populateCompanyInfo(model);
        model.addAttribute("company_Details", companyService.getAll());
        return "CompanyDetails";
    }

    @GetMapping("/admin/companyDetails/report/")
    public String getCompanyDetailsReport(Model model) {
        companyService.populateCompanyInfo(model);
        model.addAttribute("details", companyService.getAll());
        return "companyDetailsReport";
    }

    @GetMapping("/admin/edit/company/{id}/")
    public String editCompanyDetails(@PathVariable Long id, Model model) {
        companyService.populateCompanyInfo(model);
        model.addAttribute("data", companyService.getById(id));
        model.addAttribute("details", companyService.getAll());
        return "CompanyDetails";
    }

    @GetMapping("/admin/delete/company/{id}/")
    public String deleteCompanyDetails(@PathVariable Long id, Model model) {
        companyService.deleteById(id);
        model.addAttribute("details", companyService.getAll());
        return "companyDetailsReport";
    }

    // ── OFFER ─────────────────────────────────────────────────────────────────

    @GetMapping("/admin/update/offer/")
    public String setOffer(Model model) {
        companyService.populateCompanyInfo(model);
        return "ProductOffer";
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private double parseDoubleOrDefault(Object val) {
        try { return val != null ? Double.parseDouble(val.toString()) : 0.0; }
        catch (Exception e) { return 0.0; }
    }

    private float parseFloatOrDefault(Object val) {
        try { return val != null ? Float.parseFloat(val.toString()) : 0f; }
        catch (Exception e) { return 0f; }
    }
}
