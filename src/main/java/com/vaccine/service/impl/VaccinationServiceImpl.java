package com.vaccine.service.impl;

import com.vaccine.dto.VaccinationDTO;
import com.vaccine.entity.Child;
import com.vaccine.entity.Vaccination;
import com.vaccine.entity.Vaccine;
import com.vaccine.repository.ChildRepository;
import com.vaccine.repository.VaccinationRepository;
import com.vaccine.repository.VaccineRepository;
import com.vaccine.service.NotificationService;
import com.vaccine.service.VaccinationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class VaccinationServiceImpl implements VaccinationService {

    @Autowired
    private VaccinationRepository vaccinationRepository;

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private VaccineRepository vaccineRepository;
    
    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public Vaccination createVaccination(VaccinationDTO vaccinationDTO) {
        Child child = childRepository.findById(vaccinationDTO.getChildId())
                .orElseThrow(() -> new RuntimeException("Child not found with id: " + vaccinationDTO.getChildId()));
        
        Vaccine vaccine = vaccineRepository.findById(vaccinationDTO.getVaccineId())
                .orElseThrow(() -> new RuntimeException("Vaccine not found with id: " + vaccinationDTO.getVaccineId()));
        
        Vaccination vaccination = new Vaccination();
        vaccination.setChild(child);
        vaccination.setVaccine(vaccine);
        vaccination.setVaccinationDate(vaccinationDTO.getVaccinationDate());
        vaccination.setDoseNumber(vaccinationDTO.getDoseNumber());
        vaccination.setBatchNumber(vaccinationDTO.getBatchNumber());
        vaccination.setAdministeredBy(vaccinationDTO.getAdministeredBy());
        vaccination.setAdministeredAt(vaccinationDTO.getAdministeredAt());
        vaccination.setSideEffectsReported(vaccinationDTO.getSideEffectsReported());
        vaccination.setCreatedAt(LocalDateTime.now());
        
        // Calculate next dose due date if more doses are required
        if (vaccine.getDosesRequired() != null && vaccination.getDoseNumber() != null &&
                vaccination.getDoseNumber() < vaccine.getDosesRequired() && vaccine.getDaysBetweenDoses() != null) {
            LocalDate nextDoseDue = vaccination.getVaccinationDate().plusDays(vaccine.getDaysBetweenDoses());
            vaccination.setNextDoseDue(nextDoseDue);
        }
        
        Vaccination savedVaccination = vaccinationRepository.save(vaccination);
        
        // Send notification about the next dose if applicable
        if (savedVaccination.getNextDoseDue() != null) {
            notificationService.scheduleVaccinationReminder(child.getParent(), savedVaccination);
        }
        
        return savedVaccination;
    }

    @Override
    public Optional<Vaccination> findById(Long id) {
        return vaccinationRepository.findById(id);
    }

    @Override
    public List<Vaccination> findByChildId(Long childId) {
        return vaccinationRepository.findByChildId(childId);
    }

    @Override
    public List<Vaccination> findByVaccineId(Long vaccineId) {
        return vaccinationRepository.findByVaccineId(vaccineId);
    }

    @Override
    public List<Vaccination> findByChildAndVaccine(Long childId, Long vaccineId) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new RuntimeException("Child not found with id: " + childId));
        
        Vaccine vaccine = vaccineRepository.findById(vaccineId)
                .orElseThrow(() -> new RuntimeException("Vaccine not found with id: " + vaccineId));
        
        return vaccinationRepository.findByChildAndVaccine(child, vaccine);
    }

    @Override
    public List<Vaccination> findUpcomingVaccinations(LocalDate startDate, LocalDate endDate) {
        return vaccinationRepository.findUpcomingVaccinations(startDate, endDate);
    }

    @Override
    public List<Vaccination> findAllVaccinations() {
        return vaccinationRepository.findAll();
    }

    @Override
    @Transactional
    public Vaccination updateVaccination(Long id, VaccinationDTO vaccinationDTO) {
        Vaccination vaccination = vaccinationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vaccination not found with id: " + id));
        
        if (vaccinationDTO.getVaccineId() != null) {
            Vaccine vaccine = vaccineRepository.findById(vaccinationDTO.getVaccineId())
                    .orElseThrow(() -> new RuntimeException("Vaccine not found with id: " + vaccinationDTO.getVaccineId()));
            vaccination.setVaccine(vaccine);
        }
        
        if (vaccinationDTO.getVaccinationDate() != null) {
            vaccination.setVaccinationDate(vaccinationDTO.getVaccinationDate());
        }
        
        if (vaccinationDTO.getDoseNumber() != null) {
            vaccination.setDoseNumber(vaccinationDTO.getDoseNumber());
        }
        
        if (vaccinationDTO.getBatchNumber() != null) {
            vaccination.setBatchNumber(vaccinationDTO.getBatchNumber());
        }
        
        if (vaccinationDTO.getAdministeredBy() != null) {
            vaccination.setAdministeredBy(vaccinationDTO.getAdministeredBy());
        }
        
        if (vaccinationDTO.getAdministeredAt() != null) {
            vaccination.setAdministeredAt(vaccinationDTO.getAdministeredAt());
        }
        
        if (vaccinationDTO.getSideEffectsReported() != null) {
            vaccination.setSideEffectsReported(vaccinationDTO.getSideEffectsReported());
        }
        
        if (vaccinationDTO.getNextDoseDue() != null) {
            vaccination.setNextDoseDue(vaccinationDTO.getNextDoseDue());
        }
        
        vaccination.setUpdatedAt(LocalDateTime.now());
        
        return vaccinationRepository.save(vaccination);
    }

    @Override
    @Transactional
    public void deleteVaccination(Long id) {
        vaccinationRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void recordSideEffects(Long id, String sideEffects) {
        Vaccination vaccination = vaccinationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vaccination not found with id: " + id));
        
        vaccination.setSideEffectsReported(sideEffects);
        vaccination.setUpdatedAt(LocalDateTime.now());
        
        vaccinationRepository.save(vaccination);
    }

    @Override
    public List<Vaccination> getRecommendedVaccinations(Long childId) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new RuntimeException("Child not found with id: " + childId));
        
        // Calculate child's age in months
        int ageInMonths = Period.between(child.getDateOfBirth(), LocalDate.now()).getYears() * 12 +
                          Period.between(child.getDateOfBirth(), LocalDate.now()).getMonths();
        
        // Get vaccines recommended for this age
        List<Vaccine> recommendedVaccines = vaccineRepository.findVaccinesForAge(ageInMonths);
        
        List<Vaccination> recommendedVaccinations = new ArrayList<>();
        
        for (Vaccine vaccine : recommendedVaccines) {
            // Check if child has already received this vaccine
            List<Vaccination> existingVaccinations = vaccinationRepository.findByChildAndVaccine(child, vaccine);
            
            if (existingVaccinations.isEmpty()) {
                // Child hasn't received this vaccine yet
                Vaccination newVaccination = new Vaccination();
                newVaccination.setChild(child);
                newVaccination.setVaccine(vaccine);
                newVaccination.setDoseNumber(1);
                recommendedVaccinations.add(newVaccination);
            } else if (vaccine.getDosesRequired() != null && 
                       existingVaccinations.size() < vaccine.getDosesRequired()) {
                // Child needs additional doses
                Vaccination newVaccination = new Vaccination();
                newVaccination.setChild(child);
                newVaccination.setVaccine(vaccine);
                newVaccination.setDoseNumber(existingVaccinations.size() + 1);
                recommendedVaccinations.add(newVaccination);
            }
        }
        
        return recommendedVaccinations;
    }
}
