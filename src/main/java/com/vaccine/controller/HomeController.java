package com.vaccine.controller;

import com.vaccine.entity.Service;
import com.vaccine.service.FeedbackService;
import com.vaccine.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private ServiceService serviceService;
    
    @Autowired
    private FeedbackService feedbackService;

    @GetMapping("/")
    public String home(Model model) {
        List<Service> activeServices = serviceService.findActiveServices();
        double averageRating = feedbackService.calculateAverageRating();
        int highRatingsCount = feedbackService.countHighRatings(4);
        
        model.addAttribute("services", activeServices);
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("highRatingsCount", highRatingsCount);
        
        return "public/home";
    }

    @GetMapping("/services")
    public String services(Model model) {
        List<Service> activeServices = serviceService.findActiveServices();
        model.addAttribute("services", activeServices);
        
        return "public/services";
    }

    @GetMapping("/pricelist")
    public String pricelist(Model model) {
        List<Service> activeServices = serviceService.findActiveServices();
        model.addAttribute("services", activeServices);
        
        return "public/pricelist";
    }

    @GetMapping("/vaccine-guide")
    public String vaccineGuide() {
        return "public/vaccine-guide";
    }

    @GetMapping("/about")
    public String about() {
        return "public/about";
    }
}
