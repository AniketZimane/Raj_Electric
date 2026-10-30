package com.RajBattery.RajBattery.Service;

import com.RajBattery.RajBattery.Entity.InhouseInvoice;
import com.RajBattery.RajBattery.Entity.InvoiceRequest;
import com.RajBattery.RajBattery.Entity.OrderPlaced;
import com.RajBattery.RajBattery.Repo.InhouseInvoiceRepo;
import com.RajBattery.RajBattery.Repo.OrderPlacedRepo;
import com.RajBattery.RajBattery.Repo.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    @Autowired private InhouseInvoiceRepo inhouseInvoiceRepo;
    @Autowired private OrderPlacedRepo orderPlacedRepo;
    @Autowired private ProductRepo productRepo;
    @Autowired private StockService stockService;

    // ── Pending invoice items ─────────────────────────────────────────────────

    public List<InhouseInvoice> getPendingInvoices(String userName) {
        return inhouseInvoiceRepo.findByUserNameAndStatus(userName, null);
    }

    public List<InhouseInvoice> getByOrderId(Long orderId) {
        return inhouseInvoiceRepo.findByOrderId(orderId);
    }

    public void saveItem(InhouseInvoice item) {
        inhouseInvoiceRepo.save(item);
    }

    public void deleteItem(Long id) {
        inhouseInvoiceRepo.deleteById(id);
    }

    public boolean itemExists(Long id) {
        return inhouseInvoiceRepo.existsById(id);
    }

    // ── Create invoice ────────────────────────────────────────────────────────

    /**
     * Builds OrderPlaced, deducts stock via StockService.issueStock (with validation),
     * marks invoice items as completed.
     * Single unified flow — no black/white split.
     */
    @Transactional
    public String createInvoice(InvoiceRequest req) {
        // 1. Calculate amounts
        double gstAmount    = (req.getGross() * req.getGst()) / 100.0;
        double priceWithGst = req.getGross() + gstAmount;
        double discountVal  = (priceWithGst * req.getDiscount()) / 100.0;
        double netAmt       = Math.round((priceWithGst - discountVal) * 1000.0) / 1000.0;
        double paidAmount   = req.getPaidAmount() == 0 ? netAmt : req.getPaidAmount();
        double cgst         = req.getGst() / 2.0;

        // 2. Fetch pending items
        List<InhouseInvoice> invoices = getPendingInvoices(req.getUserName());
        if (invoices.isEmpty()) throw new IllegalStateException("No pending invoice items for " + req.getUserName());

        String address = invoices.stream()
                .map(InhouseInvoice::getUserAddress)
                .filter(a -> a != null && !a.isEmpty())
                .findFirst().orElse("");

        // 3. Save order
        OrderPlaced order = new OrderPlaced();
        order.setUsername(req.getUserName());
        order.setAddress(address);
        order.setGrossAmount(req.getGross());
        order.setGst(req.getGst());
        order.setCgst(cgst);
        order.setSgst(cgst);
        order.setDiscount((int) req.getDiscount());
        order.setNetAmt(netAmt);
        order.setPaymentMode(req.getPaymentMode());
        order.setPaymentStatus(req.getPaymentStatus());
        order.setPaidAmount(paidAmount);
        order.setEmailId(req.getEmailId());
        order.setMobNumber(req.getMobNumber());
        order.setVehicleNumber(req.getVehicleNumber());
        orderPlacedRepo.save(order);

        // 4. Aggregate qty per product (one stock row per product, not per invoice line)
        Map<Long, Integer> stockMap = new HashMap<>();
        for (InhouseInvoice inv : invoices) {
            stockMap.merge(inv.getProdId(), inv.getProdQty(), Integer::sum);
        }

        // 5. Deduct stock — StockService validates availability
        String invoiceRef = "INV-" + order.getId();
        for (Map.Entry<Long, Integer> entry : stockMap.entrySet()) {
            stockService.issueStock(entry.getKey(), entry.getValue(),
                    invoiceRef, "Inhouse Invoice", req.getUserName());
        }

        // 6. Mark invoice items as completed
        Long finalOrderId = order.getId();
        invoices.forEach(i -> { i.setStatus("Y"); i.setOrderId(finalOrderId); });
        inhouseInvoiceRepo.saveAll(invoices);

        return "redirect:/admin/invoice/?orderId=" + order.getId();
    }

    // ── Invoice view model ────────────────────────────────────────────────────

    public Map<String, Object> prepareInvoiceModelByOrderId(Long orderId) {
        Optional<OrderPlaced> opt = orderPlacedRepo.findById(orderId);
        if (opt.isEmpty()) return null;

        OrderPlaced input = opt.get();
        List<InhouseInvoice> invoices = getByOrderId(orderId);
        if (invoices.isEmpty()) return null;

        String invoiceDate = input.getInvoiceDate() != null
                ? input.getInvoiceDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))
                : "";

        List<Map<String, Object>> extractData = invoices.stream().map(inv -> {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id",        inv.getId());
            data.put("Prod_Name", productRepo.findPNameByPId(inv.getProdId()));
            data.put("unitPrize", productRepo.findPrizeByPId(inv.getProdId()));
            data.put("qty",       inv.getProdQty());
            data.put("amount",    inv.getProdTotalVal());
            data.put("series_no", inv.getSeriesNo());
            return data;
        }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id",            input.getId());
        result.put("grossAmount",   input.getGrossAmount());
        result.put("username",      input.getUsername());
        result.put("cgst",          input.getCgst());
        result.put("sgst",          input.getSgst());
        result.put("gst",           input.getGst());
        result.put("discount",      input.getDiscount());
        result.put("netAmt",        input.getNetAmt());
        result.put("paidAmount",    input.getPaidAmount());
        result.put("paymentMode",   input.getPaymentMode());
        result.put("paymentStatus", input.getPaymentStatus());
        result.put("mobNumber",     input.getMobNumber());
        result.put("emailId",       input.getEmailId());
        result.put("address",       input.getAddress());
        result.put("vehicle",       input.getVehicleNumber());
        result.put("extractData",   extractData);
        result.put("invoiceDate",   invoiceDate);
        return result;
    }

    /**
     * Used right after createInvoice redirect — fetches the latest order for a user.
     * @deprecated prefer prepareInvoiceModelByOrderId(orderId) for deterministic lookup.
     */
    public Map<String, Object> prepareInvoiceModelByUser(String userName) {
        List<OrderPlaced> orders = orderPlacedRepo.findByusername(userName);
        if (orders.isEmpty()) return null;
        OrderPlaced last = orders.get(orders.size() - 1);
        return prepareInvoiceModelByOrderId(last.getId());
    }
}
