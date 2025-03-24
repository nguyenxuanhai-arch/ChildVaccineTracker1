package com.vaccine.controller;

import com.vaccine.dto.ServiceDTO;
import com.vaccine.entity.Service;
import com.vaccine.entity.Vaccine;
import com.vaccine.service.ServiceService;
import com.vaccine.service.VaccineService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class ServiceController {

    @Autowired
    private ServiceService serviceService;

    @Autowired
    private VaccineService vaccineService;

    // Public pages
    @GetMapping("/services/{id}")
    public String viewServiceDetails(@PathVariable Long id, Model model) {
        Optional<Service> serviceOpt = serviceService.findById(id);
        
        if (serviceOpt.isEmpty() || !serviceOpt.get().isActive()) {
            return "redirect:/services";
        }
        
        model.addAttribute("service", serviceOpt.get());
        return "public/service-details";
    }

    // Admin management
    @GetMapping("/admin/services/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String addServiceForm(Model model) {
        model.addAttribute("serviceDTO", new ServiceDTO());
        model.addAttribute("serviceTypes", Service.ServiceType.values());
        model.addAttribute("vaccines", vaccineService.findAllVaccines());
        return "admin/service-form";
    }

    @PostMapping("/admin/services/add")
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

    @GetMapping("/admin/services/{id}/edit")
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

    @PostMapping("/admin/services/{id}/edit")
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

    // REST API endpoints
    @GetMapping("/api/services")
    @ResponseBody
    public ResponseEntity<?> getAllServices(@RequestParam(required = false) Boolean active) {
        List<Service> services;
        
        if (active != null && active) {
            services = serviceService.findActiveServices();
        } else {
            services = serviceService.findAllServices();
        }
        
        return ResponseEntity.ok(services);
    }

    @GetMapping("/api/services/{id}")
    @ResponseBody
    public ResponseEntity<?> getService(@PathVariable Long id) {
        Optional<Service> serviceOpt = serviceService.findById(id);
        
        if (serviceOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Service not found"
            ));
        }
        
        return ResponseEntity.ok(serviceOpt.get());
    }

    @GetMapping("/api/services/type/{type}")
    @ResponseBody
    public ResponseEntity<?> getServicesByType(@PathVariable String type) {
        try {
            Service.ServiceType serviceType = Service.ServiceType.valueOf(type);
            List<Service> services = serviceService.findByType(serviceType);
            return ResponseEntity.ok(services);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Invalid service type"
            ));
        }
    }

    @PostMapping("/api/services")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> createService(@Valid @RequestBody ServiceDTO serviceDTO) {
        try {
            Service service = serviceService.createService(serviceDTO);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Service created successfully",
                "service", service
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to create service: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/api/services/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> updateService(@PathVariable Long id, @Valid @RequestBody ServiceDTO serviceDTO) {
        try {
            Service service = serviceService.updateService(id, serviceDTO);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Service updated successfully",
                "service", service
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to update service: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/api/services/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> toggleServiceStatus(@PathVariable Long id) {
        try {
            serviceService.toggleServiceStatus(id);
            Optional<Service> serviceOpt = serviceService.findById(id);
            
            if (serviceOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Service not found after update"
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Service status updated successfully",
                "active", serviceOpt.get().isActive()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to update service status: " + e.getMessage()
            ));
        }
    }

    private static class Map {
        public static java.util.Map<String, Object> of(String k1, Object v1, String k2, Object v2) {
            java.util.Map<String, Object> map = new HashMap<>();
            map.put(k1, v1);
            map.put(k2, v2);
            return map;
        }
        
        public static java.util.Map<String, Object> of(String k1, Object v1, String k2, Object v2, String k3, Object v3) {
            java.util.Map<String, Object> map = new HashMap<>();
            map.put(k1, v1);
            map.put(k2, v2);
            map.put(k3, v3);
            return map;
        }
    }
}
