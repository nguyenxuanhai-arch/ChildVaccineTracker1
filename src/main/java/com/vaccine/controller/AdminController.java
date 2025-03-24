package com.vaccine.controller;

import com.vaccine.dto.ServiceDTO;
import com.vaccine.entity.Payment;
import com.vaccine.entity.Role;
import com.vaccine.entity.Service;
import com.vaccine.entity.User;
import com.vaccine.entity.Vaccine;
import com.vaccine.repository.RoleRepository;
import com.vaccine.service.AppointmentService;
import com.vaccine.service.PaymentService;
import com.vaccine.service.ServiceService;
import com.vaccine.service.UserService;
import com.vaccine.service.VaccineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ServiceService serviceService;

    @Autowired
    private VaccineService vaccineService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private PaymentService paymentService;

    // User Management
    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userService.findAllUsers();
        List<Role> roles = roleRepository.findAll();
        model.addAttribute("users", users);
        model.addAttribute("roles", roles);
        return "admin/users";
    }

    @GetMapping("/users/{id}")
    public String getUserDetails(@PathVariable Long id, Model model) {
        Optional<User> userOpt = userService.findById(id);
        if (userOpt.isEmpty()) {
            return "redirect:/admin/users";
        }
        User user = userOpt.get();
        List<Role> allRoles = roleRepository.findAll();
        model.addAttribute("user", user);
        model.addAttribute("allRoles", allRoles);
        return "admin/user-details";
    }

    @PostMapping("/users/{id}/roles")
    public String updateUserRoles(@PathVariable Long id, @RequestParam List<Long> roleIds, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userService.findById(id);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found");
            return "redirect:/admin/users";
        }

        User user = userOpt.get();
        Set<Role> roles = new HashSet<>();
        
        for (Long roleId : roleIds) {
            roleRepository.findById(roleId).ifPresent(roles::add);
        }

        user.setRoles(roles);
        userService.updateUser(user);
        
        redirectAttributes.addFlashAttribute("successMessage", "User roles updated successfully");
        return "redirect:/admin/users/" + id;
    }

    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userService.findById(id);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found");
            return "redirect:/admin/users";
        }

        User user = userOpt.get();
        user.setActive(!user.isActive());
        userService.updateUser(user);
        
        String status = user.isActive() ? "activated" : "deactivated";
        redirectAttributes.addFlashAttribute("successMessage", "User " + status + " successfully");
        return "redirect:/admin/users";
    }

    // Service Management
    @GetMapping("/services")
    public String listServices(Model model) {
        List<Service> services = serviceService.findAllServices();
        List<Vaccine> vaccines = vaccineService.findAllVaccines();
        model.addAttribute("services", services);
        model.addAttribute("vaccines", vaccines);
        model.addAttribute("serviceDTO", new ServiceDTO());
        return "admin/services";
    }

    @PostMapping("/services/add")
    public String addService(@ModelAttribute ServiceDTO serviceDTO, RedirectAttributes redirectAttributes) {
        try {
            serviceService.createService(serviceDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Service added successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add service: " + e.getMessage());
        }
        return "redirect:/admin/services";
    }

    @PostMapping("/services/{id}/update")
    public String updateService(@PathVariable Long id, @ModelAttribute ServiceDTO serviceDTO, RedirectAttributes redirectAttributes) {
        try {
            serviceService.updateService(id, serviceDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Service updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update service: " + e.getMessage());
        }
        return "redirect:/admin/services";
    }

    @PostMapping("/services/{id}/toggle")
    public String toggleServiceStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            serviceService.toggleServiceStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Service status updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update service status: " + e.getMessage());
        }
        return "redirect:/admin/services";
    }

    // Vaccine Management
    @GetMapping("/vaccines")
    public String listVaccines(Model model) {
        List<Vaccine> vaccines = vaccineService.findAllVaccines();
        model.addAttribute("vaccines", vaccines);
        model.addAttribute("vaccine", new Vaccine());
        return "admin/vaccines";
    }

    @PostMapping("/vaccines/add")
    public String addVaccine(@ModelAttribute Vaccine vaccine, RedirectAttributes redirectAttributes) {
        try {
            vaccineService.createVaccine(vaccine);
            redirectAttributes.addFlashAttribute("successMessage", "Vaccine added successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add vaccine: " + e.getMessage());
        }
        return "redirect:/admin/vaccines";
    }

    @PostMapping("/vaccines/{id}/update")
    public String updateVaccine(@PathVariable Long id, @ModelAttribute Vaccine vaccine, RedirectAttributes redirectAttributes) {
        try {
            vaccineService.updateVaccine(id, vaccine);
            redirectAttributes.addFlashAttribute("successMessage", "Vaccine updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update vaccine: " + e.getMessage());
        }
        return "redirect:/admin/vaccines";
    }

    @PostMapping("/vaccines/{id}/delete")
    public String deleteVaccine(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            vaccineService.deleteVaccine(id);
            redirectAttributes.addFlashAttribute("successMessage", "Vaccine deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete vaccine: " + e.getMessage());
        }
        return "redirect:/admin/vaccines";
    }

    // Appointments Management
    @GetMapping("/appointments")
    public String listAppointments(Model model) {
        List<com.vaccine.entity.Appointment> appointments = appointmentService.findAllAppointments();
        model.addAttribute("appointments", appointments);
        return "admin/appointments";
    }

    // Reports
    @GetMapping("/reports")
    public String reports(Model model) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        
        // Monthly revenue
        model.addAttribute("monthlyRevenue", 
            paymentService.calculateRevenueForPeriod(startOfMonth, now));
        
        // Completed appointments
        model.addAttribute("completedAppointments", 
            appointmentService.findByStatus(com.vaccine.entity.Appointment.Status.COMPLETED).size());
        
        // Pending appointments
        model.addAttribute("pendingAppointments", 
            appointmentService.findByStatus(com.vaccine.entity.Appointment.Status.SCHEDULED).size());
        
        // Total users
        model.addAttribute("totalUsers", userService.findAllUsers().size());
        
        // Successful payments
        model.addAttribute("successfulPayments", 
            paymentService.countPaymentsByStatusForPeriod(Payment.PaymentStatus.COMPLETED, startOfMonth, now));
        
        return "admin/reports";
    }

    // REST APIs for admin operations
    @PostMapping("/api/users/{id}/toggle-status")
    @ResponseBody
    public ResponseEntity<?> apiToggleUserStatus(@PathVariable Long id) {
        Optional<User> userOpt = userService.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        User user = userOpt.get();
        user.setActive(!user.isActive());
        userService.updateUser(user);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "active", user.isActive()
        ));
    }

    @PostMapping("/api/services/{id}/toggle")
    @ResponseBody
    public ResponseEntity<?> apiToggleServiceStatus(@PathVariable Long id) {
        try {
            serviceService.toggleServiceStatus(id);
            Optional<Service> serviceOpt = serviceService.findById(id);
            
            if (serviceOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Service not found after update");
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "active", serviceOpt.get().isActive()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update service status: " + e.getMessage());
        }
    }

    private static class Map {
        public static java.util.Map<String, Object> of(String k1, Object v1, String k2, Object v2) {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put(k1, v1);
            map.put(k2, v2);
            return map;
        }
    }
}
