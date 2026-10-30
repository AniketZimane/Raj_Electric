package com.RajBattery.RajBattery.Controller;

import com.RajBattery.RajBattery.Entity.Organization;
import com.RajBattery.RajBattery.Repo.OrganizationRepo;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OrganizationSetupController {

    @Autowired private OrganizationRepo orgRepo;

    // ── Admin: view/edit own org profile ─────────────────────────────────────

    @GetMapping({"/organization/setup", "/admin/organization/"})
    public String setupPage(Model model) {
        orgRepo.findFirstByOrderByIdAsc().ifPresent(org -> model.addAttribute("org", org));
        return "organizationSetup";
    }

    @PostMapping("/organization/save")
    public String saveOrganization(
            @RequestParam String orgName,
            @RequestParam String orgEmail,
            @RequestParam(required = false) String orgPhone,
            @RequestParam(required = false) String orgAddress,
            @RequestParam(required = false) String orgCity,
            @RequestParam(required = false) String orgState,
            @RequestParam(required = false) String orgPincode,
            @RequestParam(required = false) String gstNumber,
            @RequestParam(required = false) String panNumber,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String website,
            @RequestParam(required = false) MultipartFile logo,
            Model model) {
        try {
            Organization org = orgRepo.findFirstByOrderByIdAsc().orElse(new Organization());
            org.setOrgName(orgName); org.setOrgEmail(orgEmail);
            org.setOrgPhone(orgPhone); org.setOrgAddress(orgAddress);
            org.setOrgCity(orgCity); org.setOrgState(orgState);
            org.setOrgPincode(orgPincode); org.setGstNumber(gstNumber);
            org.setPanNumber(panNumber); org.setIndustry(industry);
            org.setWebsite(website); org.setSetupComplete(true);
            org.setStatus("APPROVED");
            if (logo != null && !logo.isEmpty()) org.setLogo(logo.getBytes());
            orgRepo.save(org);
            model.addAttribute("msg", "Organization profile saved successfully!");
            model.addAttribute("org", org);
        } catch (Exception e) {
            model.addAttribute("emsg", "Failed to save: " + e.getMessage());
        }
        return "organizationSetup";
    }

    // ── Public: company self-registration ────────────────────────────────────

    @GetMapping("/register-company/")
    public String registerCompanyPage() {
        return "registerCompany";
    }

    @PostMapping("/register-company/")
    public String registerCompany(
            @RequestParam String orgName,
            @RequestParam String orgEmail,
            @RequestParam(required = false) String orgPhone,
            @RequestParam(required = false) String orgAddress,
            @RequestParam(required = false) String orgCity,
            @RequestParam(required = false) String orgState,
            @RequestParam(required = false) String orgPincode,
            @RequestParam(required = false) String gstNumber,
            @RequestParam(required = false) String panNumber,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String website,
            RedirectAttributes ra) {
        if (orgRepo.existsByOrgEmail(orgEmail)) {
            ra.addFlashAttribute("emsg", "A company with this email is already registered.");
            return "redirect:/register-company/";
        }
        Organization org = new Organization();
        org.setOrgName(orgName); org.setOrgEmail(orgEmail);
        org.setOrgPhone(orgPhone); org.setOrgAddress(orgAddress);
        org.setOrgCity(orgCity); org.setOrgState(orgState);
        org.setOrgPincode(orgPincode); org.setGstNumber(gstNumber);
        org.setPanNumber(panNumber); org.setIndustry(industry);
        org.setWebsite(website); org.setStatus("PENDING");
        orgRepo.save(org);
        ra.addFlashAttribute("msg", "Registration submitted! Our team will review and approve your company shortly.");
        return "redirect:/register-company/";
    }

    // ── IT: approve / reject company ─────────────────────────────────────────

}
