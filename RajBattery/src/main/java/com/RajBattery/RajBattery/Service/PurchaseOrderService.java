package com.RajBattery.RajBattery.Service;

import com.RajBattery.RajBattery.Entity.*;
import com.RajBattery.RajBattery.Repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class PurchaseOrderService {

    @Autowired private PurchaseOrderRepo poRepo;
    @Autowired private PurchaseOrderItemRepo poItemRepo;
    @Autowired private VendorRepo vendorRepo;
    @Autowired private JavaMailSender mailSender;
    @Autowired private OrganizationRepo orgRepo;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Transactional
    public PurchaseOrder createPurchaseOrder(Long vendorId, List<Map<String, Object>> items,
                                              String notes, String createdBy) {
        Vendor vendor = vendorRepo.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found: " + vendorId));

        PurchaseOrder po = new PurchaseOrder();
        po.setPoNumber("PO-" + LocalDateTime.now().format(FMT));
        po.setVendor(vendor);
        po.setStatus("DRAFT");
        po.setNotes(notes);
        po.setCreatedBy(createdBy);

        double total = 0.0;
        List<PurchaseOrderItem> poItems = new ArrayList<>();
        for (Map<String, Object> item : items) {
            PurchaseOrderItem poi = new PurchaseOrderItem();
            poi.setPurchaseOrder(po);
            poi.setProductName(item.get("productName").toString());
            poi.setQuantity(Integer.valueOf(item.get("quantity").toString()));
            poi.setUnitRate(Double.valueOf(item.get("unitRate").toString()));
            poi.setTotalAmount(poi.getQuantity() * poi.getUnitRate());
            poi.setMatched(Boolean.parseBoolean(item.getOrDefault("matched", "false").toString()));
            if (item.get("productId") != null && !item.get("productId").toString().equals("null")) {
                poi.setProductId(Long.valueOf(item.get("productId").toString()));
            }
            total += poi.getTotalAmount();
            poItems.add(poi);
        }
        po.setTotalAmount(total);
        po.setItems(poItems);
        return poRepo.save(po);
    }

    @Transactional
    public void sendPurchaseOrder(Long poId) {
        PurchaseOrder po = poRepo.findById(poId)
                .orElseThrow(() -> new RuntimeException("PO not found: " + poId));

        sendPoEmail(po);
        po.setStatus("SENT");
        po.setSentAt(LocalDateTime.now());
        poRepo.save(po);
    }

    private void sendPoEmail(PurchaseOrder po) {
        try {
            Organization org = orgRepo.findFirstByOrderByIdAsc().orElse(null);
            String orgName = org != null ? org.getOrgName() : "Our Organization";

            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setTo(po.getVendor().getEmail());
            helper.setSubject("Purchase Order " + po.getPoNumber() + " from " + orgName);
            helper.setText(buildPoEmailHtml(po, orgName), true);
            mailSender.send(msg);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send PO email: " + e.getMessage(), e);
        }
    }

    private String buildPoEmailHtml(PurchaseOrder po, String orgName) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='font-family:Nunito,Arial,sans-serif;max-width:700px;margin:auto;border:1px solid #e0e0e0;border-radius:12px;overflow:hidden;'>");
        sb.append("<div style='background:linear-gradient(135deg,#1a237e,#3949ab);padding:24px 32px;color:white;'>");
        sb.append("<h2 style='margin:0;font-size:1.5rem;'>Purchase Order</h2>");
        sb.append("<p style='margin:4px 0 0;opacity:0.85;'>").append(po.getPoNumber()).append("</p></div>");
        sb.append("<div style='padding:24px 32px;'>");
        sb.append("<table style='width:100%;margin-bottom:16px;'><tr>");
        sb.append("<td><strong>To:</strong> ").append(po.getVendor().getVendorName()).append("<br/>");
        sb.append(po.getVendor().getAddress() != null ? po.getVendor().getAddress() : "").append("</td>");
        sb.append("<td style='text-align:right;'><strong>From:</strong> ").append(orgName).append("<br/>");
        sb.append("<strong>Date:</strong> ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).append("</td>");
        sb.append("</tr></table>");
        sb.append("<table style='width:100%;border-collapse:collapse;'>");
        sb.append("<thead><tr style='background:#f5f5f5;'>");
        sb.append("<th style='padding:10px;text-align:left;border:1px solid #e0e0e0;'>#</th>");
        sb.append("<th style='padding:10px;text-align:left;border:1px solid #e0e0e0;'>Product</th>");
        sb.append("<th style='padding:10px;text-align:center;border:1px solid #e0e0e0;'>Qty</th>");
        sb.append("<th style='padding:10px;text-align:right;border:1px solid #e0e0e0;'>Rate</th>");
        sb.append("<th style='padding:10px;text-align:right;border:1px solid #e0e0e0;'>Amount</th>");
        sb.append("</tr></thead><tbody>");
        int i = 1;
        for (PurchaseOrderItem item : po.getItems()) {
            sb.append("<tr><td style='padding:10px;border:1px solid #e0e0e0;'>").append(i++).append("</td>");
            sb.append("<td style='padding:10px;border:1px solid #e0e0e0;'>").append(item.getProductName()).append("</td>");
            sb.append("<td style='padding:10px;text-align:center;border:1px solid #e0e0e0;'>").append(item.getQuantity()).append("</td>");
            sb.append("<td style='padding:10px;text-align:right;border:1px solid #e0e0e0;'>₹").append(String.format("%.2f", item.getUnitRate())).append("</td>");
            sb.append("<td style='padding:10px;text-align:right;border:1px solid #e0e0e0;'>₹").append(String.format("%.2f", item.getTotalAmount())).append("</td></tr>");
        }
        sb.append("</tbody><tfoot><tr><td colspan='4' style='padding:10px;text-align:right;font-weight:bold;border:1px solid #e0e0e0;'>Total</td>");
        sb.append("<td style='padding:10px;text-align:right;font-weight:bold;border:1px solid #e0e0e0;'>₹").append(String.format("%.2f", po.getTotalAmount())).append("</td></tr></tfoot></table>");
        if (po.getNotes() != null && !po.getNotes().isEmpty()) {
            sb.append("<p style='margin-top:16px;'><strong>Notes:</strong> ").append(po.getNotes()).append("</p>");
        }
        sb.append("<p style='margin-top:24px;color:#666;font-size:0.85rem;'>This is a system-generated purchase order. Please acknowledge receipt.</p>");
        sb.append("</div></div>");
        return sb.toString();
    }

    public Optional<PurchaseOrder> findById(Long id) {
        return poRepo.findById(id);
    }

    public org.springframework.data.domain.Page<PurchaseOrder> getPaginated(
            org.springframework.data.domain.Pageable pageable) {
        return poRepo.findAllByOrderByCreatedAtDesc(pageable);
    }

    public void updateStatus(Long id, String status) {
        poRepo.findById(id).ifPresent(po -> { po.setStatus(status); poRepo.save(po); });
    }

    public Map<String, Long> getStatusCounts() {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String s : List.of("DRAFT", "SENT", "ACKNOWLEDGED", "COMPLETED", "CANCELLED")) {
            counts.put(s, poRepo.countByStatus(s));
        }
        return counts;
    }
}
