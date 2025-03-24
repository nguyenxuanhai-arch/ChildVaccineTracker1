package com.vaccine.controller;

import com.vaccine.dto.VaccinationDTO;
import com.vaccine.entity.Appointment;
import com.vaccine.entity.Child;
import com.vaccine.entity.User;
import com.vaccine.entity.Vaccination;
import com.vaccine.entity.Vaccine;
import com.vaccine.service.AppointmentService;
import com.vaccine.service.ChildService;
import com.vaccine.service.UserService;
import com.vaccine.service.VaccinationService;
import com.vaccine.service.VaccineService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class VaccinationController {

    @Autowired
    private VaccinationService vaccinationService;

    @Autowired
    private ChildService childService;

    @Autowired
    private VaccineService vaccineService;

    @Autowired
    private UserService userService;

    @Autowired
    private AppointmentService appointmentService;

    // Customer views
    @GetMapping("/customer/vaccinations")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String listVaccinations(Model model) {
        User currentUser = getCurrentUser();
        List<Child> children = childService.findByParent(currentUser);
        model.addAttribute("children", children);
        return "customer/vaccination-children";
    }

    @GetMapping("/customer/vaccinations/child/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String viewChildVaccinations(@PathVariable Long id, Model model) {
        User currentUser = getCurrentUser();
        Optional<Child> childOpt = childService.findById(id);
        
        if (childOpt.isEmpty() || !childService.isParentOfChild(currentUser, id)) {
            return "redirect:/customer/vaccinations";
        }
        
        Child child = childOpt.get();
        List<Vaccination> vaccinations = vaccinationService.findByChildId(id);
        List<Vaccination> recommendedVaccinations = vaccinationService.getRecommendedVaccinations(id);
        
        model.addAttribute("child", child);
        model.addAttribute("vaccinations", vaccinations);
        model.addAttribute("recommendedVaccinations", recommendedVaccinations);
        
        return "customer/vaccination-history";
    }

    @GetMapping("/customer/vaccinations/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String viewVaccinationDetails(@PathVariable Long id, Model model) {
        User currentUser = getCurrentUser();
        Optional<Vaccination> vaccinationOpt = vaccinationService.findById(id);
        
        if (vaccinationOpt.isEmpty()) {
            return "redirect:/customer/vaccinations";
        }
        
        Vaccination vaccination = vaccinationOpt.get();
        
        // Check if the child belongs to the current user
        if (!childService.isParentOfChild(currentUser, vaccination.getChild().getId())) {
            return "redirect:/customer/vaccinations";
        }
        
        model.addAttribute("vaccination", vaccination);
        return "customer/vaccination-details";
    }

    @PostMapping("/customer/vaccinations/{id}/side-effects")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String reportSideEffects(@PathVariable Long id,
                                  @RequestParam String sideEffects,
                                  RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        Optional<Vaccination> vaccinationOpt = vaccinationService.findById(id);
        
        if (vaccinationOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vaccination record not found");
            return "redirect:/customer/vaccinations";
        }
        
        Vaccination vaccination = vaccinationOpt.get();
        
        // Check if the child belongs to the current user
        if (!childService.isParentOfChild(currentUser, vaccination.getChild().getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "You do not have permission to report side effects for this vaccination");
            return "redirect:/customer/vaccinations";
        }
        
        try {
            vaccinationService.recordSideEffects(id, sideEffects);
            redirectAttributes.addFlashAttribute("successMessage", "Side effects reported successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to report side effects: " + e.getMessage());
        }
        
        return "redirect:/customer/vaccinations/" + id;
    }

    // Staff views
    @GetMapping("/staff/vaccinations/record")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public String recordVaccinationForm(Model model) {
        model.addAttribute("vaccinationDTO", new VaccinationDTO());
        model.addAttribute("children", childService.findAllChildren());
        model.addAttribute("vaccines", vaccineService.findAllVaccines());
        
        // Get today's appointments for reference
        java.time.LocalDateTime today = java.time.LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        java.time.LocalDateTime tomorrow = today.plusDays(1);
        List<Appointment> todayAppointments = appointmentService.findByDateRange(today, tomorrow);
        model.addAttribute("todayAppointments", todayAppointments);
        
        return "staff/vaccination-record";
    }

    @PostMapping("/staff/vaccinations/record")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public String recordVaccination(@Valid @ModelAttribute("vaccinationDTO") VaccinationDTO vaccinationDTO,
                                  BindingResult result,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        if (result.hasErrors()) {
            model.addAttribute("children", childService.findAllChildren());
            model.addAttribute("vaccines", vaccineService.findAllVaccines());
            return "staff/vaccination-record";
        }
        
        try {
            Vaccination vaccination = vaccinationService.createVaccination(vaccinationDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Vaccination recorded successfully");
            return "redirect:/staff/vaccinations";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to record vaccination: " + e.getMessage());
            return "redirect:/staff/vaccinations/record";
        }
    }

    @GetMapping("/staff/vaccinations")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public String listAllVaccinations(Model model) {
        List<Vaccination> vaccinations = vaccinationService.findAllVaccinations();
        model.addAttribute("vaccinations", vaccinations);
        return "staff/vaccinations";
    }

    @GetMapping("/staff/vaccinations/child/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public String viewChildVaccinationsStaff(@PathVariable Long id, Model model) {
        Optional<Child> childOpt = childService.findById(id);
        
        if (childOpt.isEmpty()) {
            return "redirect:/staff/vaccinations";
        }
        
        Child child = childOpt.get();
        List<Vaccination> vaccinations = vaccinationService.findByChildId(id);
        List<Vaccination> recommendedVaccinations = vaccinationService.getRecommendedVaccinations(id);
        
        model.addAttribute("child", child);
        model.addAttribute("vaccinations", vaccinations);
        model.addAttribute("recommendedVaccinations", recommendedVaccinations);
        
        return "staff/child-vaccinations";
    }

    @GetMapping("/staff/vaccinations/{id}/edit")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public String editVaccinationForm(@PathVariable Long id, Model model) {
        Optional<Vaccination> vaccinationOpt = vaccinationService.findById(id);
        
        if (vaccinationOpt.isEmpty()) {
            return "redirect:/staff/vaccinations";
        }
        
        Vaccination vaccination = vaccinationOpt.get();
        VaccinationDTO vaccinationDTO = new VaccinationDTO();
        vaccinationDTO.setId(vaccination.getId());
        vaccinationDTO.setChildId(vaccination.getChild().getId());
        vaccinationDTO.setVaccineId(vaccination.getVaccine().getId());
        vaccinationDTO.setVaccineName(vaccination.getVaccine().getName());
        vaccinationDTO.setVaccinationDate(vaccination.getVaccinationDate());
        vaccinationDTO.setDoseNumber(vaccination.getDoseNumber());
        vaccinationDTO.setBatchNumber(vaccination.getBatchNumber());
        vaccinationDTO.setAdministeredBy(vaccination.getAdministeredBy());
        vaccinationDTO.setAdministeredAt(vaccination.getAdministeredAt());
        vaccinationDTO.setNextDoseDue(vaccination.getNextDoseDue());
        vaccinationDTO.setSideEffectsReported(vaccination.getSideEffectsReported());
        
        model.addAttribute("vaccinationDTO", vaccinationDTO);
        model.addAttribute("children", childService.findAllChildren());
        model.addAttribute("vaccines", vaccineService.findAllVaccines());
        
        return "staff/vaccination-edit";
    }

    @PostMapping("/staff/vaccinations/{id}/edit")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public String updateVaccination(@PathVariable Long id,
                                  @Valid @ModelAttribute("vaccinationDTO") VaccinationDTO vaccinationDTO,
                                  BindingResult result,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        if (result.hasErrors()) {
            model.addAttribute("children", childService.findAllChildren());
            model.addAttribute("vaccines", vaccineService.findAllVaccines());
            return "staff/vaccination-edit";
        }
        
        try {
            vaccinationService.updateVaccination(id, vaccinationDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Vaccination record updated successfully");
            return "redirect:/staff/vaccinations";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update vaccination record: " + e.getMessage());
            return "redirect:/staff/vaccinations/" + id + "/edit";
        }
    }

    @PostMapping("/staff/vaccinations/{id}/delete")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public String deleteVaccination(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            vaccinationService.deleteVaccination(id);
            redirectAttributes.addFlashAttribute("successMessage", "Vaccination record deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete vaccination record: " + e.getMessage());
        }
        
        return "redirect:/staff/vaccinations";
    }

    // REST API endpoints
    @GetMapping("/api/vaccinations/child/{childId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'ADMIN')")
    @ResponseBody
    public ResponseEntity<?> getChildVaccinations(@PathVariable Long childId) {
        User currentUser = getCurrentUser();
        
        // Check if current user is the parent of the child (or staff/admin)
        boolean isParent = childService.isParentOfChild(currentUser, childId);
        boolean isStaffOrAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName() == com.vaccine.entity.Role.RoleName.ROLE_STAFF 
                        || r.getName() == com.vaccine.entity.Role.RoleName.ROLE_ADMIN);
        
        if (!isParent && !isStaffOrAdmin) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "You do not have permission to view this child's vaccinations"
            ));
        }
        
        Optional<Child> childOpt = childService.findById(childId);
        
        if (childOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Child not found"
            ));
        }
        
        List<Vaccination> vaccinations = vaccinationService.findByChildId(childId);
        return ResponseEntity.ok(vaccinations);
    }

    @GetMapping("/api/vaccinations/recommended/{childId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'ADMIN')")
    @ResponseBody
    public ResponseEntity<?> getRecommendedVaccinations(@PathVariable Long childId) {
        User currentUser = getCurrentUser();
        
        // Check if current user is the parent of the child (or staff/admin)
        boolean isParent = childService.isParentOfChild(currentUser, childId);
        boolean isStaffOrAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName() == com.vaccine.entity.Role.RoleName.ROLE_STAFF 
                        || r.getName() == com.vaccine.entity.Role.RoleName.ROLE_ADMIN);
        
        if (!isParent && !isStaffOrAdmin) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "You do not have permission to view this child's recommended vaccinations"
            ));
        }
        
        Optional<Child> childOpt = childService.findById(childId);
        
        if (childOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Child not found"
            ));
        }
        
        List<Vaccination> recommendedVaccinations = vaccinationService.getRecommendedVaccinations(childId);
        return ResponseEntity.ok(recommendedVaccinations);
    }

    @PostMapping("/api/vaccinations")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @ResponseBody
    public ResponseEntity<?> createVaccination(@Valid @RequestBody VaccinationDTO vaccinationDTO) {
        try {
            Vaccination vaccination = vaccinationService.createVaccination(vaccinationDTO);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Vaccination record created successfully",
                "vaccination", vaccination
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to create vaccination record: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/api/vaccinations/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @ResponseBody
    public ResponseEntity<?> updateVaccination(@PathVariable Long id, @Valid @RequestBody VaccinationDTO vaccinationDTO) {
        try {
            Vaccination vaccination = vaccinationService.updateVaccination(id, vaccinationDTO);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Vaccination record updated successfully",
                "vaccination", vaccination
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to update vaccination record: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/api/vaccinations/{id}/side-effects")
    @PreAuthorize("hasRole('CUSTOMER')")
    @ResponseBody
    public ResponseEntity<?> reportSideEffects(@PathVariable Long id, @RequestParam String sideEffects) {
        User currentUser = getCurrentUser();
        Optional<Vaccination> vaccinationOpt = vaccinationService.findById(id);
        
        if (vaccinationOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Vaccination record not found"
            ));
        }
        
        Vaccination vaccination = vaccinationOpt.get();
        
        // Check if the child belongs to the current user
        if (!childService.isParentOfChild(currentUser, vaccination.getChild().getId())) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "You do not have permission to report side effects for this vaccination"
            ));
        }
        
        try {
            vaccinationService.recordSideEffects(id, sideEffects);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Side effects reported successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to report side effects: " + e.getMessage()
            ));
        }
    }

    // Helper methods
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Current user not found"));
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
