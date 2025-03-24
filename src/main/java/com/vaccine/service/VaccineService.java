package com.vaccine.service;

import com.vaccine.entity.Vaccine;

import java.util.List;
import java.util.Optional;

public interface VaccineService {
    
    Vaccine createVaccine(Vaccine vaccine);
    
    Optional<Vaccine> findById(Long id);
    
    List<Vaccine> findAllVaccines();
    
    List<Vaccine> findRequiredVaccines();
    
    List<Vaccine> findVaccinesForAge(Integer ageInMonths);
    
    Vaccine updateVaccine(Long id, Vaccine vaccine);
    
    void deleteVaccine(Long id);
}
