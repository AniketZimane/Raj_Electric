package com.RajBattery.RajBattery.Controller;

import com.RajBattery.RajBattery.Entity.Category;
import com.RajBattery.RajBattery.Entity.CompanyDetails;
import com.RajBattery.RajBattery.Entity.Product;
import com.RajBattery.RajBattery.Repo.CategoryRepo;
import com.RajBattery.RajBattery.Repo.CompanyDetailsRepo;
import com.RajBattery.RajBattery.Repo.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class UserController {

    @Autowired
    ProductRepo productRepo;
    @Autowired
    CategoryRepo categoryRepo;
    @Autowired
    CompanyDetailsRepo companyDetailsRepo;
    @GetMapping("/admin/dashboard/")
    public String getHome(Model model)
    {
        List<CompanyDetails> companyDetails=companyDetailsRepo.findAll();
        for (CompanyDetails comp:companyDetails) {
            model.addAttribute("Cname",comp.getcName());
        }
        return "adminDashboard";
    }

    @GetMapping("/login/")
    public String getLogin(Model model)
    {

        return "login";
    }

    @GetMapping("/new/Product/")
    public String getNewProduct(Model model)
    {
        List<Category> categoryList=categoryRepo.findAll();
        model.addAttribute("categoryList",categoryList);
        return "ProductForm";
    }

}
