package com.vaccine.controller;

import com.vaccine.dto.FeedbackDTO;
import com.vaccine.entity.Feedback;
import com.vaccine.entity.User;
import com.vaccine.service.FeedbackService;
import com.vaccine.service.UserService;
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
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private UserService userService;

    @GetMapping("/customer/feedback")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String feedbackForm(Model model) {
        User currentUser = getCurrentUser();
        
        // Get user's existing feedbacks
        List<Feedback> feedbacks = feedbackService.findByUserId(currentUser.getId());
        model.addAttribute("feedbacks", feedbacks);
        
        // Prepare new feedback form
        model.addAttribute("feedbackDTO", new FeedbackDTO());
        
        return "customer/feedback";
    }

    @PostMapping("/customer/feedback")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String submitFeedback(@Valid @ModelAttribute("feedbackDTO") FeedbackDTO feedbackDTO,
                                BindingResult result,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "customer/feedback";
        }

        User currentUser = getCurrentUser();
        
        try {
            feedbackService.createFeedback(feedbackDTO, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Thank you for your feedback!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to submit feedback: " + e.getMessage());
        }
        
        return "redirect:/customer/feedback";
    }

    @GetMapping("/customer/feedback/{id}/edit")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String editFeedbackForm(@PathVariable Long id, Model model) {
        User currentUser = getCurrentUser();
        Optional<Feedback> feedbackOpt = feedbackService.findById(id);
        
        if (feedbackOpt.isEmpty() || !feedbackOpt.get().getUser().getId().equals(currentUser.getId())) {
            return "redirect:/customer/feedback";
        }
        
        Feedback feedback = feedbackOpt.get();
        FeedbackDTO feedbackDTO = new FeedbackDTO();
        feedbackDTO.setId(feedback.getId());
        feedbackDTO.setComment(feedback.getComment());
        feedbackDTO.setRating(feedback.getRating());
        
        model.addAttribute("feedbackDTO", feedbackDTO);
        model.addAttribute("isEdit", true);
        
        return "customer/feedback-edit";
    }

    @PostMapping("/customer/feedback/{id}/edit")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String updateFeedback(@PathVariable Long id,
                               @Valid @ModelAttribute("feedbackDTO") FeedbackDTO feedbackDTO,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "customer/feedback-edit";
        }

        User currentUser = getCurrentUser();
        
        try {
            feedbackService.updateFeedback(id, feedbackDTO, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Feedback updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update feedback: " + e.getMessage());
        }
        
        return "redirect:/customer/feedback";
    }

    @PostMapping("/customer/feedback/{id}/delete")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String deleteFeedback(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        Optional<Feedback> feedbackOpt = feedbackService.findById(id);
        
        if (feedbackOpt.isEmpty() || !feedbackOpt.get().getUser().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "You cannot delete this feedback");
            return "redirect:/customer/feedback";
        }
        
        try {
            feedbackService.deleteFeedback(id);
            redirectAttributes.addFlashAttribute("successMessage", "Feedback deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete feedback: " + e.getMessage());
        }
        
        return "redirect:/customer/feedback";
    }

    // Admin feedback management
    @GetMapping("/admin/feedbacks")
    @PreAuthorize("hasRole('ADMIN')")
    public String listFeedbacks(Model model) {
        List<Feedback> feedbacks = feedbackService.findAllFeedbacks();
        model.addAttribute("feedbacks", feedbacks);
        model.addAttribute("averageRating", feedbackService.calculateAverageRating());
        model.addAttribute("highRatings", feedbackService.countHighRatings(4));
        return "admin/feedbacks";
    }

    @PostMapping("/admin/feedbacks/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDeleteFeedback(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            feedbackService.deleteFeedback(id);
            redirectAttributes.addFlashAttribute("successMessage", "Feedback deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete feedback: " + e.getMessage());
        }
        
        return "redirect:/admin/feedbacks";
    }

    // REST API endpoints
    @GetMapping("/api/feedbacks")
    @ResponseBody
    public ResponseEntity<?> getAllFeedbacks() {
        List<Feedback> feedbacks = feedbackService.findAllFeedbacks();
        return ResponseEntity.ok(feedbacks);
    }

    @PostMapping("/api/feedbacks")
    @PreAuthorize("hasRole('CUSTOMER')")
    @ResponseBody
    public ResponseEntity<?> createFeedback(@Valid @RequestBody FeedbackDTO feedbackDTO) {
        User currentUser = getCurrentUser();
        
        try {
            Feedback feedback = feedbackService.createFeedback(feedbackDTO, currentUser);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Feedback submitted successfully",
                "feedback", feedback
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to submit feedback: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/api/feedbacks/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @ResponseBody
    public ResponseEntity<?> updateFeedback(@PathVariable Long id, @Valid @RequestBody FeedbackDTO feedbackDTO) {
        User currentUser = getCurrentUser();
        Optional<Feedback> feedbackOpt = feedbackService.findById(id);
        
        if (feedbackOpt.isEmpty() || !feedbackOpt.get().getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "You cannot update this feedback"
            ));
        }
        
        try {
            Feedback feedback = feedbackService.updateFeedback(id, feedbackDTO, currentUser);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Feedback updated successfully",
                "feedback", feedback
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to update feedback: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/api/feedbacks/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @ResponseBody
    public ResponseEntity<?> deleteFeedback(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        Optional<Feedback> feedbackOpt = feedbackService.findById(id);
        
        // Admin can delete any feedback, customers can only delete their own
        if (feedbackOpt.isEmpty() || 
            (!currentUser.getRoles().stream().anyMatch(r -> r.getName() == com.vaccine.entity.Role.RoleName.ROLE_ADMIN) && 
             !feedbackOpt.get().getUser().getId().equals(currentUser.getId()))) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "You cannot delete this feedback"
            ));
        }
        
        try {
            feedbackService.deleteFeedback(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Feedback deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to delete feedback: " + e.getMessage()
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
    
    private static class Role {
        public enum RoleName {
            ROLE_ADMIN,
            ROLE_STAFF,
            ROLE_CUSTOMER,
            ROLE_GUEST
        }
    }
}
