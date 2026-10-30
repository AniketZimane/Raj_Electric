package com.RajBattery.RajBattery.Service;

import com.RajBattery.RajBattery.Entity.CompanyDetails;
import com.RajBattery.RajBattery.Repo.CompanyDetailsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.List;

@Service
public class CompanyService {

    @Autowired
    private CompanyDetailsRepo companyDetailsRepo;

    public void populateCompanyInfo(Model model) {
        List<CompanyDetails> list = companyDetailsRepo.findAll();
        if (!list.isEmpty()) {
            CompanyDetails comp = list.get(0);
            model.addAttribute("Cname", comp.getcName());
            model.addAttribute("Cadd", comp.getcAddress());
            model.addAttribute("Cnumber", comp.getcNumber());
            model.addAttribute("gstNumber", comp.getGstNumber());
        }
    }

    public CompanyDetails getFirst() {
        List<CompanyDetails> list = companyDetailsRepo.findAll();
        return list.isEmpty() ? null : list.get(0);
    }

    public List<CompanyDetails> getAll() {
        return companyDetailsRepo.findAll();
    }

    public void save(CompanyDetails companyDetails) {
        companyDetailsRepo.save(companyDetails);
    }

    public void deleteById(Long id) {
        companyDetailsRepo.deleteById(id);
    }

    public CompanyDetails getById(Long id) {
        return companyDetailsRepo.getReferenceById(id);
    }
}
