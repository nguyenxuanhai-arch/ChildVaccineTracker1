package com.vaccine.service.impl;

import com.vaccine.dto.PaymentDTO;
import com.vaccine.entity.Payment;
import com.vaccine.entity.Appointment;
import com.vaccine.repository.PaymentRepository;
import com.vaccine.repository.AppointmentRepository;
import com.vaccine.service.NotificationService;
import com.vaccine.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public Payment createPayment(PaymentDTO paymentDTO) {
        Payment payment = new Payment();
        payment.setAmount(paymentDTO.getAmount());
        payment.setPaymentMethod(paymentDTO.getPaymentMethod());
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());

        Appointment appointment = appointmentRepository.findById(paymentDTO.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        payment.setAppointment(appointment);

        return paymentRepository.save(payment);
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return paymentRepository.findById(id);
    }

    @Override
    public List<Payment> findByAppointmentId(Long appointmentId) {
        return paymentRepository.findByAppointmentId(appointmentId);
    }

    @Override
    public List<Payment> findAllPayments() {
        return paymentRepository.findAll();
    }

    @Override
    @Transactional
    public Payment updatePaymentStatus(Long id, Payment.PaymentStatus status) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStatus(status);
        payment.setUpdatedAt(LocalDateTime.now());

        return paymentRepository.save(payment);
    }
    
    @Override
    @Transactional
    public Payment updatePaymentStatus(Long paymentId, Payment.PaymentStatus status) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));
        payment.setStatus(status);
        payment.setUpdatedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public void deletePayment(Long id) {
        paymentRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Payment processPayment(Long paymentId, String transactionId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));
        if (payment.getStatus() != Payment.PaymentStatus.PENDING) {
            throw new RuntimeException("Payment is not in PENDING status");
        }
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setTransactionId(transactionId);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        Payment completedPayment = paymentRepository.save(payment);
        String title = "Payment Received";
        String message = "Your payment of " + payment.getAmount() + " for appointment on " + 
                         payment.getAppointment().getAppointmentDate().toLocalDate() + 
                         " has been processed successfully.";
        notificationService.createNotification(
            payment.getAppointment().getChild().getParent(),
            title,
            message,
            com.vaccine.entity.Notification.NotificationType.SYSTEM_NOTIFICATION
        );
        return completedPayment;
    }

    @Override
    @Transactional
    public Payment refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));
        if (payment.getStatus() != Payment.PaymentStatus.COMPLETED) {
            throw new RuntimeException("Only completed payments can be refunded");
        }
        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        payment.setUpdatedAt(LocalDateTime.now());
        Payment refundedPayment = paymentRepository.save(payment);
        String title = "Payment Refunded";
        String message = "Your payment of " + payment.getAmount() + " for appointment on " + 
                         payment.getAppointment().getAppointmentDate().toLocalDate() + 
                         " has been refunded.";
        notificationService.createNotification(
            payment.getAppointment().getChild().getParent(),
            title,
            message,
            com.vaccine.entity.Notification.NotificationType.SYSTEM_NOTIFICATION
        );
        return refundedPayment;
    }

    @Override
    public BigDecimal calculateRevenueForPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal revenue = paymentRepository.sumAmountByStatusAndDateRange(Payment.PaymentStatus.COMPLETED, startDate, endDate);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    @Override
    public int countPaymentsByStatusForPeriod(Payment.PaymentStatus status, LocalDateTime startDate, LocalDateTime endDate) {
        Integer count = paymentRepository.countByStatusAndDateRange(status, startDate, endDate);
        return count != null ? count : 0;
    }
}