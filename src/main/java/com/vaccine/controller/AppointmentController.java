package com.vaccine.controller;

import com.vaccine.dto.AppointmentDTO;
import com.vaccine.entity.Appointment;
import com.vaccine.entity.Child;
import com.vaccine.entity.Payment;
import com.vaccine.entity.Service;
import com.vaccine.entity.User;
import com.vaccine.service.AppointmentService;
import com.vaccine.service.ChildService;
import com.vaccine.service.PaymentService;
import com.vaccine.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private ChildService childService;

    @Autowired
    private ServiceService serviceService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private com.vaccine.service.UserService userService;

    // Customer appointment booking
    @GetMapping("/customer/appointments/book")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String bookAppointmentForm(Model model) {
        User currentUser = getCurrentUser();
        List<Child> children = childService.findByParent(currentUser);
        List<Service> services = serviceService.findActiveServices();

        if (children.isEmpty()) {
            model.addAttribute("warning", "Please add a child profile first before booking an appointment.");
            return "customer/children";
        }

        model.addAttribute("appointmentDTO", new AppointmentDTO());
        model.addAttribute("children", children);
        model.addAttribute("services", services);
        return "customer/appointment-booking";
    }

    @PostMapping("/customer/appointments/book")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String bookAppointment(@Valid @ModelAttribute AppointmentDTO appointmentDTO,
                                  BindingResult result,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        User currentUser = getCurrentUser();

        if (result.hasErrors()) {
            List<Child> children = childService.findByParent(currentUser);
            List<Service> services = serviceService.findActiveServices();
            model.addAttribute("children", children);
            model.addAttribute("services", services);
            return "customer/appointment-booking";
        }

        try {
            Appointment appointment = appointmentService.createAppointment(appointmentDTO, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Appointment booked successfully. We look forward to seeing you!");
            return "redirect:/customer/appointments";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to book appointment: " + e.getMessage());
            return "redirect:/customer/appointments/book";
        }
    }

    @GetMapping("/customer/appointments")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String listCustomerAppointments(Model model) {
        User currentUser = getCurrentUser();
        List<Appointment> appointments = appointmentService.findByParentId(currentUser.getId());
        model.addAttribute("appointments", appointments);
        return "customer/appointments";
    }

    @GetMapping("/customer/appointments/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String appointmentDetails(@PathVariable Long id, Model model) {
        User currentUser = getCurrentUser();
        Optional<Appointment> appointmentOpt = appointmentService.findById(id);
        
        if (appointmentOpt.isEmpty()) {
            return "redirect:/customer/appointments";
        }
        
        Appointment appointment = appointmentOpt.get();
        
        // Check if the appointment belongs to one of the user's children
        boolean isParentOfChild = childService.isParentOfChild(currentUser, appointment.getChild().getId());
        if (!isParentOfChild) {
            return "redirect:/customer/appointments";
        }
        
        model.addAttribute("appointment", appointment);
        
        // Check if the appointment can be cancelled
        model.addAttribute("canCancel", appointmentService.canCancelAppointment(id, currentUser));
        
        // Get payment details if exists
        Optional<Payment> paymentOpt = paymentService.findByAppointmentId(id);
        paymentOpt.ifPresent(payment -> model.addAttribute("payment", payment));
        
        return "customer/appointment-details";
    }

    @PostMapping("/customer/appointments/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String cancelAppointment(@PathVariable Long id,
                                   @RequestParam String reason,
                                   RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        
        try {
            if (!appointmentService.canCancelAppointment(id, currentUser)) {
                redirectAttributes.addFlashAttribute("errorMessage", "This appointment cannot be cancelled. It may be too close to the appointment time or already completed.");
                return "redirect:/customer/appointments/" + id;
            }
            
            appointmentService.cancelAppointment(id, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Appointment cancelled successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to cancel appointment: " + e.getMessage());
        }
        
        return "redirect:/customer/appointments";
    }

    // Staff appointment management
    @GetMapping("/staff/appointments")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public String listStaffAppointments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Appointment.Status status,
            Model model) {
        
        List<Appointment> appointments;
        
        if (startDate != null && endDate != null) {
            appointments = appointmentService.findByDateRange(startDate, endDate);
        } else if (status != null) {
            appointments = appointmentService.findByStatus(status);
        } else {
            // Default: show today's appointments
            LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            LocalDateTime tomorrow = today.plusDays(1);
            appointments = appointmentService.findByDateRange(today, tomorrow);
        }
        
        model.addAttribute("appointments", appointments);
        model.addAttribute("statuses", Appointment.Status.values());
        model.addAttribute("currentStatus", status);
        
        return "staff/appointment-management";
    }

    @PostMapping("/staff/appointments/{id}/status")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public String updateAppointmentStatus(@PathVariable Long id,
                                         @RequestParam Appointment.Status status,
                                         RedirectAttributes redirectAttributes) {
        try {
            appointmentService.updateAppointmentStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Appointment status updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update appointment status: " + e.getMessage());
        }
        
        return "redirect:/staff/appointments";
    }

    @PostMapping("/staff/appointments/{id}/complete-payment")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public String completePayment(@PathVariable Long id,
                                 @RequestParam Payment.PaymentMethod paymentMethod,
                                 @RequestParam(required = false) String transactionId,
                                 RedirectAttributes redirectAttributes) {
        try {
            Optional<Appointment> appointmentOpt = appointmentService.findById(id);
            if (appointmentOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Appointment not found");
                return "redirect:/staff/appointments";
            }
            
            Appointment appointment = appointmentOpt.get();
            
            // Create payment if it doesn't exist
            Optional<Payment> existingPayment = paymentService.findByAppointmentId(id);
            
            if (existingPayment.isEmpty()) {
                Payment payment = paymentService.createPayment(
                    appointment, 
                    appointment.getService().getPrice(), 
                    paymentMethod
                );
                
                paymentService.processPayment(payment.getId(), transactionId);
            } else {
                // Update existing payment
                Payment payment = existingPayment.get();
                payment.setMethod(paymentMethod);
                payment.setStatus(Payment.PaymentStatus.COMPLETED);
                payment.setTransactionId(transactionId);
                payment.setPaymentDate(LocalDateTime.now());
                paymentService.updatePaymentStatus(payment.getId(), Payment.PaymentStatus.COMPLETED);
            }
            
            // Mark appointment as completed if it's not already
            if (appointment.getStatus() != Appointment.Status.COMPLETED) {
                appointmentService.updateAppointmentStatus(id, Appointment.Status.COMPLETED);
            }
            
            redirectAttributes.addFlashAttribute("successMessage", "Payment processed and appointment completed successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to process payment: " + e.getMessage());
        }
        
        return "redirect:/staff/appointments";
    }

    // API endpoints
    @PostMapping("/api/appointments/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    @ResponseBody
    public ResponseEntity<?> apiCancelAppointment(@PathVariable Long id, @RequestParam String reason) {
        User currentUser = getCurrentUser();
        
        if (!appointmentService.canCancelAppointment(id, currentUser)) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "This appointment cannot be cancelled"
            ));
        }
        
        try {
            Appointment appointment = appointmentService.cancelAppointment(id, reason);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Appointment cancelled successfully",
                "appointment", appointment
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to cancel appointment: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/api/appointments/{id}/status")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @ResponseBody
    public ResponseEntity<?> apiUpdateAppointmentStatus(@PathVariable Long id, @RequestParam Appointment.Status status) {
        try {
            Appointment appointment = appointmentService.updateAppointmentStatus(id, status);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Appointment status updated successfully",
                "appointment", appointment
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to update appointment status: " + e.getMessage()
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
