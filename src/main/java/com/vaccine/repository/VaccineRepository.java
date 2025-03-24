package com.vaccine.repository;

import com.vaccine.entity.Vaccine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VaccineRepository extends JpaRepository<Vaccine, Long> {
    
    List<Vaccine> findByRequired(Boolean required);
    
    @Query("SELECT v FROM Vaccine v WHERE v.minAgeMonths <= :ageInMonths AND (v.maxAgeMonths IS NULL OR v.maxAgeMonths >= :ageInMonths)")
    List<Vaccine> findVaccinesForAge(@Param("ageInMonths") Integer ageInMonths);
}
