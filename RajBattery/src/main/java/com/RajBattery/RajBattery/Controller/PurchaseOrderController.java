package com.RajBattery.RajBattery.Controller;

import com.RajBattery.RajBattery.Entity.*;
import com.RajBattery.RajBattery.Service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class PurchaseOrderController {

    @Autowired private PurchaseOrderService poService;
    @Autowired private VendorService vendorService;
    @Autowired private CompanyService companyService;
    @Autowired private HttpSession session;

    // ─── VENDOR MANAGEMENT ───────────────────────────────────────────────────

    @GetMapping("/vendor/list/")
    public String vendorList(Model model) {
        companyService.populateCompanyInfo(model);
        model.addAttribute("vendors", vendorService.getAll());
        return "vendorManagement";
    }

    @PostMapping("/vendor/save/")
    public String saveVendor(@RequestParam String vendorName,
                             @RequestParam String email,
                             @RequestParam(required = false) String phone,
                             @RequestParam(required = false) String address,
                             @RequestParam(required = false) String gstNumber,
                             @RequestParam(required = false) String contactPerson,
                             Model model) {
        companyService.populateCompanyInfo(model);
        if (vendorService.emailExists(email)) {
            model.addAttribute("emsg", "Vendor with this email already exists.");
            model.addAttribute("vendors", vendorService.getAll());
            return "vendorManagement";
        }
        Vendor v = new Vendor();
        v.setVendorName(vendorName);
        v.setEmail(email);
        v.setPhone(phone);
        v.setAddress(address);
        v.setGstNumber(gstNumber);
        v.setContactPerson(contactPerson);
        vendorService.save(v);
        model.addAttribute("msg", "Vendor saved successfully.");
        model.addAttribute("vendors", vendorService.getAll());
        return "vendorManagement";
    }

    @GetMapping("/vendor/deactivate/{id}/")
    public String deactivateVendor(@PathVariable Long id) {
        vendorService.deactivate(id);
        return "redirect:/vendor/list/";
    }

    @GetMapping("/api/vendors/search")
    @ResponseBody
    public List<Map<String, Object>> searchVendors(@RequestParam String query) {
        return vendorService.search(query).stream().map(v -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", v.getId());
            m.put("vendorName", v.getVendorName());
            m.put("email", v.getEmail());
            m.put("phone", v.getPhone());
            return m;
        }).collect(java.util.stream.Collectors.toList());
    }

    // ─── PRODUCT MATCHING API ─────────────────────────────────────────────────

    /**
     * Accepts a list of product names, returns match results + vendor suggestions for unmatched.
     */
    @PostMapping("/api/purchase-order/match-products/")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> matchProducts(
            @RequestBody Map<String, Object> body) {
        try {
            @SuppressWarnings("unchecked")
            List<String> names = (List<String>) body.get("productNames");
            if (names == null || names.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            List<Map<String, Object>> results = vendorService.matchProductsToVendors(names);
            // For unmatched items, also attach available active vendors
            List<Map<String, Object>> activeVendors = vendorService.getAllActive().stream().map(v -> {
                Map<String, Object> vm = new LinkedHashMap<>();
                vm.put("id", v.getId());
                vm.put("vendorName", v.getVendorName());
                vm.put("email", v.getEmail());
                return vm;
            }).collect(java.util.stream.Collectors.toList());

            results.forEach(r -> {
                if (!(boolean) r.get("matched")) {
                    r.put("availableVendors", activeVendors);
                }
            });
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ─── PURCHASE ORDER CRUD ─────────────────────────────────────────────────

    @GetMapping("/purchase-order/new/")
    public String newPurchaseOrder(Model model) {
        companyService.populateCompanyInfo(model);
        model.addAttribute("vendors", vendorService.getAllActive());
        return "purchaseOrder";
    }

    @PostMapping("/api/purchase-order/create/")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createPurchaseOrder(
            @RequestBody Map<String, Object> body) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            Long vendorId = Long.valueOf(body.get("vendorId").toString());
            String notes = body.getOrDefault("notes", "").toString();
            String createdBy = session.getAttribute("key") != null
                    ? session.getAttribute("key").toString() : "Admin";

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");

            if (items == null || items.isEmpty()) {
                response.put("error", "No items provided.");
                return ResponseEntity.badRequest().body(response);
            }

            PurchaseOrder po = poService.createPurchaseOrder(vendorId, items, notes, createdBy);
            response.put("success", true);
            response.put("poId", po.getId());
            response.put("poNumber", po.getPoNumber());
            response.put("status", po.getStatus());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/api/purchase-order/send/{id}/")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendPurchaseOrder(@PathVariable Long id) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            poService.sendPurchaseOrder(id);
            response.put("success", true);
            response.put("message", "Purchase order sent to vendor successfully.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Failed to send PO: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping({"/purchase-orders/", "/purchase-order/list/"})
    public String purchaseOrderList(@RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    Model model) {
        companyService.populateCompanyInfo(model);
        Page<PurchaseOrder> poPage = poService.getPaginated(
                PageRequest.of(page - 1, size, Sort.by("createdAt").descending()));
        model.addAttribute("poList", poPage.getContent());
        model.addAttribute("curPage", page);
        model.addAttribute("totalPages", poPage.getTotalPages());
        model.addAttribute("recordSize", poPage.getTotalElements());
        model.addAttribute("statusCounts", poService.getStatusCounts());
        return "purchaseOrderList";
    }

    @GetMapping("/purchase-order/view/{id}/")
    public String viewPurchaseOrder(@PathVariable Long id, Model model) {
        companyService.populateCompanyInfo(model);
        PurchaseOrder po = poService.findById(id)
                .orElseThrow(() -> new RuntimeException("PO not found"));
        model.addAttribute("po", po);
        return "purchaseOrderView";
    }

    @PostMapping("/purchase-order/status/{id}/")
    public String updateStatus(@PathVariable Long id, @RequestParam String status) {
        poService.updateStatus(id, status);
        return "redirect:/purchase-order/list/";
    }
}
