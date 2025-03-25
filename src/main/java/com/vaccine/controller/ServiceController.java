package com.vaccine.controller;

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vaccine.dto.ServiceDTO;
import com.vaccine.entity.Service;
import com.vaccine.entity.Vaccine;
import com.vaccine.service.ServiceService;
import com.vaccine.service.VaccineService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/services")
public class ServiceController {

    @Autowired
    private ServiceService serviceService;

    @Autowired
    private VaccineService vaccineService;

    // Public pages
    @GetMapping("/{id}")
    public String viewServiceDetails(@PathVariable Long id, Model model) {
        Optional<Service> serviceOpt = serviceService.findById(id);
        
        if (serviceOpt.isEmpty() || !serviceOpt.get().isActive()) {
            return "redirect:/services";
        }
        
        model.addAttribute("service", serviceOpt.get());
        return "public/service-details";
    }

    // Admin management
    @GetMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String addServiceForm(Model model) {
        model.addAttribute("serviceDTO", new ServiceDTO());
        model.addAttribute("serviceTypes", Service.ServiceType.values());
        model.addAttribute("vaccines", vaccineService.findAllVaccines());
        return "admin/service-form";
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String addService(@Valid @ModelAttribute("serviceDTO") ServiceDTO serviceDTO,
                            BindingResult result,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("serviceTypes", Service.ServiceType.values());
            model.addAttribute("vaccines", vaccineService.findAllVaccines());
            return "admin/service-form";
        }
        
        try {
            serviceService.createService(serviceDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Service added successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add service: " + e.getMessage());
        }
        
        return "redirect:/admin/services";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editServiceForm(@PathVariable Long id, Model model) {
        Optional<Service> serviceOpt = serviceService.findById(id);
        
        if (serviceOpt.isEmpty()) {
            return "redirect:/admin/services";
        }
        
        Service service = serviceOpt.get();
        ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setId(service.getId());
        serviceDTO.setName(service.getName());
        serviceDTO.setDescription(service.getDescription());
        serviceDTO.setType(service.getType());
        serviceDTO.setPrice(service.getPrice());
        serviceDTO.setActive(service.isActive());
        serviceDTO.setVaccineIds(service.getVaccines().stream()
                                .map(Vaccine::getId)
                                .collect(Collectors.toList()));
        
        model.addAttribute("serviceDTO", serviceDTO);
        model.addAttribute("serviceTypes", Service.ServiceType.values());
        model.addAttribute("vaccines", vaccineService.findAllVaccines());
        return "admin/service-form";
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateService(@PathVariable Long id,
                              @Valid @ModelAttribute("serviceDTO") ServiceDTO serviceDTO,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (result.hasErrors()) {
            model.addAttribute("serviceTypes", Service.ServiceType.values());
            model.addAttribute("vaccines", vaccineService.findAllVaccines());
            return "admin/service-form";
        }
        
        try {
            serviceService.updateService(id, serviceDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Service updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update service: " + e.getMessage());
        }
        
        return "redirect:/admin/services";
    }
}
