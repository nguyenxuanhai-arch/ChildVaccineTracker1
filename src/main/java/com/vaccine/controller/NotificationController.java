package com.vaccine.controller;

import com.vaccine.entity.Notification;
import com.vaccine.entity.User;
import com.vaccine.service.NotificationService;
import com.vaccine.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @GetMapping("/customer/notifications")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String listNotifications(Model model) {
        User currentUser = getCurrentUser();
        List<Notification> notifications = notificationService.findByUserId(currentUser.getId());
        model.addAttribute("notifications", notifications);
        return "customer/notifications";
    }

    @PostMapping("/customer/notifications/{id}/mark-read")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String markNotificationAsRead(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        Optional<Notification> notificationOpt = notificationService.findById(id);
        
        if (notificationOpt.isEmpty() || !notificationOpt.get().getUser().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Notification not found");
            return "redirect:/customer/notifications";
        }
        
        try {
            notificationService.markAsRead(id);
            redirectAttributes.addFlashAttribute("successMessage", "Notification marked as read");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to mark notification as read: " + e.getMessage());
        }
        
        return "redirect:/customer/notifications";
    }

    @PostMapping("/customer/notifications/mark-all-read")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String markAllNotificationsAsRead(RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        
        try {
            notificationService.markAllAsRead(currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "All notifications marked as read");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to mark all notifications as read: " + e.getMessage());
        }
        
        return "redirect:/customer/notifications";
    }

    @PostMapping("/customer/notifications/{id}/delete")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String deleteNotification(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        Optional<Notification> notificationOpt = notificationService.findById(id);
        
        if (notificationOpt.isEmpty() || !notificationOpt.get().getUser().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Notification not found");
            return "redirect:/customer/notifications";
        }
        
        try {
            notificationService.deleteNotification(id);
            redirectAttributes.addFlashAttribute("successMessage", "Notification deleted");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete notification: " + e.getMessage());
        }
        
        return "redirect:/customer/notifications";
    }

    // For staff to send notifications
    @GetMapping("/staff/notifications/send")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public String sendNotificationForm(Model model) {
        List<User> users = userService.findAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("notificationTypes", Notification.NotificationType.values());
        return "staff/send-notification";
    }

    @PostMapping("/staff/notifications/send")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public String sendNotification(@RequestParam Long userId,
                                  @RequestParam String title,
                                  @RequestParam String message,
                                  @RequestParam Notification.NotificationType type,
                                  RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userService.findById(userId);
        
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found");
            return "redirect:/staff/notifications/send";
        }
        
        try {
            notificationService.createNotification(userOpt.get(), title, message, type);
            redirectAttributes.addFlashAttribute("successMessage", "Notification sent successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to send notification: " + e.getMessage());
        }
        
        return "redirect:/staff/notifications/send";
    }

    // REST API endpoints
    @GetMapping("/api/notifications")
    @PreAuthorize("hasRole('CUSTOMER')")
    @ResponseBody
    public ResponseEntity<?> getNotifications() {
        User currentUser = getCurrentUser();
        List<Notification> notifications = notificationService.findByUserId(currentUser.getId());
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/api/notifications/unread")
    @PreAuthorize("hasRole('CUSTOMER')")
    @ResponseBody
    public ResponseEntity<?> getUnreadNotifications() {
        User currentUser = getCurrentUser();
        List<Notification> unreadNotifications = notificationService.findUnreadByUserId(currentUser.getId());
        return ResponseEntity.ok(unreadNotifications);
    }

    @GetMapping("/api/notifications/count")
    @PreAuthorize("hasRole('CUSTOMER')")
    @ResponseBody
    public ResponseEntity<?> getUnreadCount() {
        User currentUser = getCurrentUser();
        int unreadCount = notificationService.getUnreadCount(currentUser.getId());
        return ResponseEntity.ok(Map.of(
            "unreadCount", unreadCount
        ));
    }

    @PostMapping("/api/notifications/{id}/read")
    @PreAuthorize("hasRole('CUSTOMER')")
    @ResponseBody
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        Optional<Notification> notificationOpt = notificationService.findById(id);
        
        if (notificationOpt.isEmpty() || !notificationOpt.get().getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Notification not found"
            ));
        }
        
        try {
            notificationService.markAsRead(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Notification marked as read"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to mark notification as read: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/api/notifications/read-all")
    @PreAuthorize("hasRole('CUSTOMER')")
    @ResponseBody
    public ResponseEntity<?> markAllAsRead() {
        User currentUser = getCurrentUser();
        
        try {
            notificationService.markAllAsRead(currentUser.getId());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "All notifications marked as read"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to mark all notifications as read: " + e.getMessage()
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
        public static java.util.Map<String, Object> of(String k1, Object v1) {
            java.util.Map<String, Object> map = new HashMap<>();
            map.put(k1, v1);
            return map;
        }
        
        public static java.util.Map<String, Object> of(String k1, Object v1, String k2, Object v2) {
            java.util.Map<String, Object> map = new HashMap<>();
            map.put(k1, v1);
            map.put(k2, v2);
            return map;
        }
    }
}
