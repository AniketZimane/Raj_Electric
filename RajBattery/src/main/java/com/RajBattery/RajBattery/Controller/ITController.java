package com.RajBattery.RajBattery.Controller;

import com.RajBattery.RajBattery.Entity.Organization;
import com.RajBattery.RajBattery.Entity.Registration;
import com.RajBattery.RajBattery.Repo.OrganizationRepo;
import com.RajBattery.RajBattery.Service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/it")
public class ITController {

    @Autowired private UserService userService;
    @Autowired private OrganizationRepo organizationRepo;
    @Autowired private HttpSession session;

    // ── Dashboard ─────────────────────────────────────────────────────────────

    @GetMapping("/dashboard/")
    public String dashboard(Model model) {
        List<Registration> pending     = userService.getPendingRegistrations();
        List<Registration> all         = userService.getAllRegistrations();
        List<Organization> approvedOrgs = organizationRepo.findByStatusOrderByCreatedAtDesc("APPROVED");
        List<Organization> pendingOrgs  = organizationRepo.findByStatusOrderByCreatedAtDesc("PENDING");
        List<Organization> allOrgs      = organizationRepo.findAllByOrderByCreatedAtDesc();

        model.addAttribute("pendingList",   pending);
        model.addAttribute("allList",       all);
        model.addAttribute("approvedOrgs",  approvedOrgs);   // for company dropdown in approval form
        model.addAttribute("pendingOrgs",   pendingOrgs);
        model.addAttribute("allOrgs",       allOrgs);
        model.addAttribute("pendingCount",    pending.size());
        model.addAttribute("pendingOrgCount", pendingOrgs.size());
        return "itDashboard";
    }

    // ── User registration approve / reject ────────────────────────────────────

    @PostMapping("/approve/{id}/")
    public String approve(@PathVariable Integer id,
                          @RequestParam(defaultValue = "0") String userType,
                          @RequestParam(required = false) Long companyId,
                          RedirectAttributes ra) {
        userService.approve(id, (String) session.getAttribute("key"), userType, companyId);
        ra.addFlashAttribute("msg", "Registration approved successfully.");
        return "redirect:/it/dashboard/";
    }

    @PostMapping("/reject/{id}/")
    public String reject(@PathVariable Integer id, RedirectAttributes ra) {
        userService.reject(id, (String) session.getAttribute("key"));
        ra.addFlashAttribute("emsg", "Registration rejected.");
        return "redirect:/it/dashboard/";
    }

    // ── Edit company mapping for an already-approved user ────────────────────

    @PostMapping("/user/remap/{id}/")
    public String remapUser(@PathVariable Integer id,
                            @RequestParam(required = false) Long companyId,
                            @RequestParam(defaultValue = "0") String userType,
                            RedirectAttributes ra) {
        userService.approve(id, (String) session.getAttribute("key"), userType, companyId);
        ra.addFlashAttribute("msg", "User mapping updated.");
        return "redirect:/it/dashboard/";
    }

    // ── Company approve / reject ──────────────────────────────────────────────

    @PostMapping("/company/approve/{id}/")
    public String approveCompany(@PathVariable Long id, RedirectAttributes ra) {
        String by = (String) session.getAttribute("key");
        organizationRepo.findById(id).ifPresent(org -> {
            org.setStatus("APPROVED");
            org.setApprovedBy(by);
            org.setApprovedAt(LocalDateTime.now());
            organizationRepo.save(org);
        });
        ra.addFlashAttribute("msg", "Company approved successfully.");
        return "redirect:/it/dashboard/";
    }

    @PostMapping("/company/reject/{id}/")
    public String rejectCompany(@PathVariable Long id, RedirectAttributes ra) {
        String by = (String) session.getAttribute("key");
        organizationRepo.findById(id).ifPresent(org -> {
            org.setStatus("REJECTED");
            org.setApprovedBy(by);
            org.setApprovedAt(LocalDateTime.now());
            organizationRepo.save(org);
        });
        ra.addFlashAttribute("emsg", "Company registration rejected.");
        return "redirect:/it/dashboard/";
    }
}
