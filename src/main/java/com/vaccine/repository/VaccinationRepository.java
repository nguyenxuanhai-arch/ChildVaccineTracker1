package com.vaccine.repository;

import com.vaccine.entity.Child;
import com.vaccine.entity.Vaccination;
import com.vaccine.entity.Vaccine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VaccinationRepository extends JpaRepository<Vaccination, Long> {
    
    List<Vaccination> findByChild(Child child);
    
    List<Vaccination> findByChildId(Long childId);
    
    List<Vaccination> findByVaccine(Vaccine vaccine);
    
    List<Vaccination> findByVaccineId(Long vaccineId);
    
    List<Vaccination> findByChildAndVaccine(Child child, Vaccine vaccine);
    
    @Query("SELECT v FROM Vaccination v WHERE v.child.id = :childId AND v.vaccine.id = :vaccineId ORDER BY v.doseNumber DESC")
    List<Vaccination> findLatestVaccinationForChildAndVaccine(@Param("childId") Long childId, @Param("vaccineId") Long vaccineId);
    
    @Query("SELECT v FROM Vaccination v WHERE v.nextDoseDue BETWEEN :startDate AND :endDate")
    List<Vaccination> findUpcomingVaccinations(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(v) FROM Vaccination v WHERE v.vaccinationDate BETWEEN :startDate AND :endDate")
    Integer countVaccinationsInDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
