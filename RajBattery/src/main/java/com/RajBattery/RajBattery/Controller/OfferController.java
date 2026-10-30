package com.RajBattery.RajBattery.Controller;

import com.RajBattery.RajBattery.Entity.Offer;
import com.RajBattery.RajBattery.Service.CompanyService;
import com.RajBattery.RajBattery.Service.OfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/offers")
public class OfferController {

    @Autowired private OfferService offerService;
    @Autowired private CompanyService companyService;

    @PostMapping("/upload")
    public String uploadOffer(Model model,
                              @RequestParam("offerName") String offerName,
                              @RequestParam("description") String description,
                              @RequestParam("endDate") String endDate,
                              @RequestParam("banner") MultipartFile banner) {
        companyService.populateCompanyInfo(model);
        try {
            Offer offer = new Offer();
            offer.setOfferName(offerName);
            offer.setDescription(description);
            offer.setEndDate(endDate);
            offer.setBanner(banner.getBytes());
            offerService.saveOffer(offer);
            model.addAttribute("msg", "Offer uploaded successfully");
        } catch (Exception e) {
            model.addAttribute("emsg", "Banner size is too large");
        }
        return "ProductOffer";
    }

    @GetMapping("/{id}/banner")
    public ResponseEntity<byte[]> getOfferBanner(@PathVariable Long id) {
        Offer offer = offerService.getOfferById(id);
        if (offer == null || offer.getBanner() == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(offer.getBanner());
    }
}
