package com.vaccine.service;

import com.vaccine.dto.VaccinationDTO;
import com.vaccine.entity.Vaccination;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VaccinationService {
    
    Vaccination createVaccination(VaccinationDTO vaccinationDTO);
    
    Optional<Vaccination> findById(Long id);
    
    List<Vaccination> findByChildId(Long childId);
    
    List<Vaccination> findByVaccineId(Long vaccineId);
    
    List<Vaccination> findByChildAndVaccine(Long childId, Long vaccineId);
    
    List<Vaccination> findUpcomingVaccinations(LocalDate startDate, LocalDate endDate);
    
    List<Vaccination> findAllVaccinations();
    
    Vaccination updateVaccination(Long id, VaccinationDTO vaccinationDTO);
    
    void deleteVaccination(Long id);
    
    void recordSideEffects(Long id, String sideEffects);
    
    List<Vaccination> getRecommendedVaccinations(Long childId);
}
