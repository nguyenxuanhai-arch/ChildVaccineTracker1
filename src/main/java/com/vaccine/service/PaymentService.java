package com.vaccine.service;

import com.vaccine.dto.PaymentDTO;
import com.vaccine.entity.Appointment;
import com.vaccine.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentService {
    
    Payment createPayment(Appointment appointment, BigDecimal amount, Payment.PaymentMethod method);
    
    Optional<Payment> findById(Long id);
    
    Optional<Payment> findByAppointmentId(Long appointmentId);
    
    List<Payment> findByStatus(Payment.PaymentStatus status);
    
    List<Payment> findAllPayments();
    
    Payment updatePaymentStatus(Long paymentId, Payment.PaymentStatus status);
    
    Payment processPayment(Long paymentId, String transactionId);
    
    Payment refundPayment(Long paymentId);
    
    BigDecimal calculateRevenueForPeriod(LocalDateTime startDate, LocalDateTime endDate);

    Payment createPayment(PaymentDTO paymentDTO);

    void deletePayment(Long id);
    
    int countPaymentsByStatusForPeriod(Payment.PaymentStatus status, LocalDateTime startDate, LocalDateTime endDate);
}
