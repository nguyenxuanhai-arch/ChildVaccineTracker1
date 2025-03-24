package com.vaccine.controller;

import com.vaccine.entity.Appointment;
import com.vaccine.entity.Payment;
import com.vaccine.service.AppointmentService;
import com.vaccine.service.ChildService;
import com.vaccine.service.FeedbackService;
import com.vaccine.service.PaymentService;
import com.vaccine.service.UserService;
import com.vaccine.service.VaccinationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private ChildService childService;

    @Autowired
    private UserService userService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private VaccinationService vaccinationService;

    @Autowired
    private FeedbackService feedbackService;

    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard(Model model) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfDay = now.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        // Today's appointments
        List<Appointment> todayAppointments = appointmentService.findByDateRange(startOfDay, endOfDay);
        model.addAttribute("todayAppointments", todayAppointments);
        model.addAttribute("pendingAppointments", appointmentService.findByStatus(Appointment.Status.SCHEDULED).size());
        model.addAttribute("completedAppointments", appointmentService.findByStatus(Appointment.Status.COMPLETED).size());

        // Revenue statistics
        model.addAttribute("dailyRevenue", paymentService.calculateRevenueForPeriod(startOfDay, endOfDay));
        model.addAttribute("monthlyRevenue", paymentService.calculateRevenueForPeriod(startOfMonth, now));

        // User statistics
        model.addAttribute("totalUsers", userService.findAllUsers().size());
        model.addAttribute("totalChildren", childService.findAllChildren().size());

        // Feedback statistics
        model.addAttribute("averageRating", feedbackService.calculateAverageRating());
        model.addAttribute("highRatings", feedbackService.countHighRatings(4));

        return "admin/dashboard";
    }

    @GetMapping("/staff/dashboard")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public String staffDashboard(Model model) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        // Today's appointments
        List<Appointment> todayAppointments = appointmentService.findByDateRange(startOfDay, endOfDay);
        model.addAttribute("todayAppointments", todayAppointments);
        
        // Upcoming vaccinations
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);
        model.addAttribute("upcomingVaccinations", vaccinationService.findUpcomingVaccinations(today, nextWeek));

        return "staff/dashboard";
    }

    @GetMapping("/customer/dashboard")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String customerDashboard(Model model) {
        // Get current user
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        com.vaccine.entity.User currentUser = userService.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get user's children
        model.addAttribute("children", childService.findByParent(currentUser));
        
        // Get upcoming appointments
        List<Appointment> appointments = appointmentService.findByParentId(currentUser.getId());
        model.addAttribute("upcomingAppointments", appointments.stream()
                .filter(a -> a.getStatus() == Appointment.Status.SCHEDULED || a.getStatus() == Appointment.Status.CONFIRMED)
                .limit(5)
                .toList());

        // Get unread notifications count
        com.vaccine.service.NotificationService notificationService = 
            org.springframework.beans.factory.annotation.Autowired.class.cast(
                org.springframework.context.ApplicationContext.class.cast(
                    org.springframework.web.context.support.WebApplicationContextUtils.getWebApplicationContext(
                        jakarta.servlet.ServletContext.class.cast(
                            org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes().resolveReference("request")
                        ).getServletContext()
                    )
                ).getBean("notificationService")
            );
        
        model.addAttribute("unreadNotifications", notificationService.getUnreadCount(currentUser.getId()));

        return "customer/dashboard";
    }

    // API endpoints for dashboard data
    @GetMapping("/api/admin/dashboard/revenue-chart")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public Map<String, Object> getRevenueChartData() {
        LocalDateTime now = LocalDateTime.now();
        List<BigDecimal> revenueData = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        // Get last 6 months of revenue data
        for (int i = 5; i >= 0; i--) {
            YearMonth yearMonth = YearMonth.from(now.minusMonths(i));
            LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);
            
            BigDecimal revenue = paymentService.calculateRevenueForPeriod(startOfMonth, endOfMonth);
            revenueData.add(revenue);
            labels.add(yearMonth.getMonth().toString() + " " + yearMonth.getYear());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("revenueData", revenueData);
        return result;
    }

    @GetMapping("/api/admin/dashboard/appointment-chart")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public Map<String, Object> getAppointmentChartData() {
        LocalDateTime now = LocalDateTime.now();
        List<Integer> completedData = new ArrayList<>();
        List<Integer> scheduledData = new ArrayList<>();
        List<Integer> cancelledData = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        // Get last 6 months of appointment data
        for (int i = 5; i >= 0; i--) {
            YearMonth yearMonth = YearMonth.from(now.minusMonths(i));
            LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);
            
            Integer completed = appointmentService.countByStatusAndDateRange(
                Appointment.Status.COMPLETED, startOfMonth, endOfMonth);
            Integer scheduled = appointmentService.countByStatusAndDateRange(
                Appointment.Status.SCHEDULED, startOfMonth, endOfMonth);
            Integer cancelled = appointmentService.countByStatusAndDateRange(
                Appointment.Status.CANCELLED, startOfMonth, endOfMonth);
            
            completedData.add(completed);
            scheduledData.add(scheduled);
            cancelledData.add(cancelled);
            labels.add(yearMonth.getMonth().toString());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("completedData", completedData);
        result.put("scheduledData", scheduledData);
        result.put("cancelledData", cancelledData);
        return result;
    }

    @GetMapping("/api/admin/dashboard/payment-chart")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public Map<String, Object> getPaymentChartData() {
        LocalDateTime now = LocalDateTime.now();
        YearMonth yearMonth = YearMonth.from(now);
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        
        // Get payment distribution by method for current month
        Map<Payment.PaymentMethod, Integer> paymentsByMethod = new HashMap<>();
        for (Payment.PaymentMethod method : Payment.PaymentMethod.values()) {
            List<Payment> payments = paymentService.findByStatus(Payment.PaymentStatus.COMPLETED);
            int count = (int) payments.stream()
                    .filter(p -> p.getMethod() == method && 
                           p.getPaymentDate() != null &&
                           p.getPaymentDate().isAfter(startOfMonth) && 
                           p.getPaymentDate().isBefore(endOfMonth))
                    .count();
            paymentsByMethod.put(method, count);
        }

        List<String> labels = new ArrayList<>();
        List<Integer> data = new ArrayList<>();
        
        for (Map.Entry<Payment.PaymentMethod, Integer> entry : paymentsByMethod.entrySet()) {
            labels.add(entry.getKey().toString());
            data.add(entry.getValue());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("data", data);
        return result;
    }
}
