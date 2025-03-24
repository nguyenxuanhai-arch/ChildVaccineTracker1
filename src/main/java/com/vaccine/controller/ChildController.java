package com.vaccine.controller;

import com.vaccine.dto.ChildDTO;
import com.vaccine.entity.Child;
import com.vaccine.entity.User;
import com.vaccine.service.ChildService;
import com.vaccine.service.UserService;
import com.vaccine.service.VaccinationService;
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
public class ChildController {

    @Autowired
    private ChildService childService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private VaccinationService vaccinationService;

    @GetMapping("/customer/children")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String listChildren(Model model) {
        User currentUser = getCurrentUser();
        List<Child> children = childService.findByParent(currentUser);
        model.addAttribute("children", children);
        return "customer/children";
    }

    @GetMapping("/customer/children/add")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String addChildForm(Model model) {
        model.addAttribute("childDTO", new ChildDTO());
        model.addAttribute("genders", Child.Gender.values());
        return "customer/child-form";
    }

    @PostMapping("/customer/children/add")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String addChild(@Valid @ModelAttribute("childDTO") ChildDTO childDTO,
                           BindingResult result,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        if (result.hasErrors()) {
            model.addAttribute("genders", Child.Gender.values());
            return "customer/child-form";
        }

        User currentUser = getCurrentUser();
        
        try {
            childService.createChild(childDTO, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Child profile added successfully");
            return "redirect:/customer/children";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add child: " + e.getMessage());
            return "redirect:/customer/children/add";
        }
    }

    @GetMapping("/customer/children/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String childDetails(@PathVariable Long id, Model model) {
        User currentUser = getCurrentUser();
        Optional<Child> childOpt = childService.findById(id);
        
        if (childOpt.isEmpty() || !childService.isParentOfChild(currentUser, id)) {
            return "redirect:/customer/children";
        }
        
        Child child = childOpt.get();
        model.addAttribute("child", child);
        
        // Get recommended vaccinations
        model.addAttribute("recommendedVaccinations", vaccinationService.getRecommendedVaccinations(id));
        
        // Get vaccination history
        model.addAttribute("vaccinationHistory", vaccinationService.findByChildId(id));
        
        return "customer/child-profile";
    }

    @GetMapping("/customer/children/{id}/edit")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String editChildForm(@PathVariable Long id, Model model) {
        User currentUser = getCurrentUser();
        Optional<Child> childOpt = childService.findById(id);
        
        if (childOpt.isEmpty() || !childService.isParentOfChild(currentUser, id)) {
            return "redirect:/customer/children";
        }
        
        Child child = childOpt.get();
        ChildDTO childDTO = new ChildDTO();
        childDTO.setId(child.getId());
        childDTO.setFullName(child.getFullName());
        childDTO.setDateOfBirth(child.getDateOfBirth());
        childDTO.setGender(child.getGender());
        childDTO.setBloodType(child.getBloodType());
        childDTO.setWeight(child.getWeight());
        childDTO.setHeight(child.getHeight());
        childDTO.setAllergies(child.getAllergies());
        childDTO.setMedicalConditions(child.getMedicalConditions());
        
        model.addAttribute("childDTO", childDTO);
        model.addAttribute("genders", Child.Gender.values());
        
        return "customer/child-form";
    }

    @PostMapping("/customer/children/{id}/edit")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String updateChild(@PathVariable Long id,
                             @Valid @ModelAttribute("childDTO") ChildDTO childDTO,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        User currentUser = getCurrentUser();
        
        if (!childService.isParentOfChild(currentUser, id)) {
            return "redirect:/customer/children";
        }
        
        if (result.hasErrors()) {
            model.addAttribute("genders", Child.Gender.values());
            return "customer/child-form";
        }
        
        try {
            childService.updateChild(id, childDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Child profile updated successfully");
            return "redirect:/customer/children/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update child: " + e.getMessage());
            return "redirect:/customer/children/" + id + "/edit";
        }
    }

    @PostMapping("/customer/children/{id}/delete")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String deleteChild(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        
        if (!childService.isParentOfChild(currentUser, id)) {
            redirectAttributes.addFlashAttribute("errorMessage", "You do not have permission to delete this child profile");
            return "redirect:/customer/children";
        }
        
        try {
            childService.deleteChild(id);
            redirectAttributes.addFlashAttribute("successMessage", "Child profile deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete child: " + e.getMessage());
        }
        
        return "redirect:/customer/children";
    }

    // Staff view
    @GetMapping("/staff/children")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public String listAllChildren(Model model) {
        List<Child> children = childService.findAllChildren();
        model.addAttribute("children", children);
        return "staff/children";
    }

    @GetMapping("/staff/children/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public String staffChildDetails(@PathVariable Long id, Model model) {
        Optional<Child> childOpt = childService.findById(id);
        
        if (childOpt.isEmpty()) {
            return "redirect:/staff/children";
        }
        
        Child child = childOpt.get();
        model.addAttribute("child", child);
        
        // Get vaccination history
        model.addAttribute("vaccinationHistory", vaccinationService.findByChildId(id));
        
        // Get recommended vaccinations
        model.addAttribute("recommendedVaccinations", vaccinationService.getRecommendedVaccinations(id));
        
        return "staff/child-detail";
    }

    // REST API endpoints
    @GetMapping("/api/children")
    @PreAuthorize("hasRole('CUSTOMER')")
    @ResponseBody
    public ResponseEntity<?> getChildren() {
        User currentUser = getCurrentUser();
        List<Child> children = childService.findByParent(currentUser);
        return ResponseEntity.ok(children);
    }

    @GetMapping("/api/children/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @ResponseBody
    public ResponseEntity<?> getChild(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        
        if (!childService.isParentOfChild(currentUser, id)) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "You do not have permission to access this child profile"
            ));
        }
        
        Optional<Child> childOpt = childService.findById(id);
        
        if (childOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Child not found"
            ));
        }
        
        return ResponseEntity.ok(childOpt.get());
    }

    @PostMapping("/api/children")
    @PreAuthorize("hasRole('CUSTOMER')")
    @ResponseBody
    public ResponseEntity<?> createChild(@Valid @RequestBody ChildDTO childDTO) {
        User currentUser = getCurrentUser();
        
        try {
            Child child = childService.createChild(childDTO, currentUser);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Child profile created successfully",
                "child", child
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to create child profile: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/api/children/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @ResponseBody
    public ResponseEntity<?> updateChild(@PathVariable Long id, @Valid @RequestBody ChildDTO childDTO) {
        User currentUser = getCurrentUser();
        
        if (!childService.isParentOfChild(currentUser, id)) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "You do not have permission to update this child profile"
            ));
        }
        
        try {
            Child child = childService.updateChild(id, childDTO);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Child profile updated successfully",
                "child", child
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to update child profile: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/api/children/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @ResponseBody
    public ResponseEntity<?> deleteChild(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        
        if (!childService.isParentOfChild(currentUser, id)) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "You do not have permission to delete this child profile"
            ));
        }
        
        try {
            childService.deleteChild(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Child profile deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to delete child profile: " + e.getMessage()
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
