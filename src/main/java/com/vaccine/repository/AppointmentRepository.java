package com.vaccine.repository;

import com.vaccine.entity.Appointment;
import com.vaccine.entity.Child;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    List<Appointment> findByChild(Child child);
    
    List<Appointment> findByChildId(Long childId);
    
    List<Appointment> findByStatus(Appointment.Status status);
    
    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate BETWEEN :startDate AND :endDate")
    List<Appointment> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM Appointment a WHERE a.child.parent.id = :parentId")
    List<Appointment> findByParentId(@Param("parentId") Long parentId);
    
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = :status AND a.appointmentDate BETWEEN :startDate AND :endDate")
    Integer countByStatusAndDateRange(@Param("status") Appointment.Status status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
