package com.vaccine.service.impl;

import com.vaccine.entity.Vaccine;
import com.vaccine.repository.VaccineRepository;
import com.vaccine.service.VaccineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class VaccineServiceImpl implements VaccineService {

    @Autowired
    private VaccineRepository vaccineRepository;

    @Override
    @Transactional
    public Vaccine createVaccine(Vaccine vaccine) {
        return vaccineRepository.save(vaccine);
    }

    @Override
    public Optional<Vaccine> findById(Long id) {
        return vaccineRepository.findById(id);
    }

    @Override
    public List<Vaccine> findAllVaccines() {
        return vaccineRepository.findAll();
    }

    @Override
    public List<Vaccine> findRequiredVaccines() {
        return vaccineRepository.findByRequired(true);
    }

    @Override
    public List<Vaccine> findVaccinesForAge(Integer ageInMonths) {
        return vaccineRepository.findVaccinesForAge(ageInMonths);
    }

    @Override
    @Transactional
    public Vaccine updateVaccine(Long id, Vaccine vaccineDetails) {
        Vaccine vaccine = vaccineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vaccine not found with id: " + id));
        
        vaccine.setName(vaccineDetails.getName());
        vaccine.setManufacturer(vaccineDetails.getManufacturer());
        vaccine.setDescription(vaccineDetails.getDescription());
        vaccine.setMinAgeMonths(vaccineDetails.getMinAgeMonths());
        vaccine.setMaxAgeMonths(vaccineDetails.getMaxAgeMonths());
        vaccine.setDosesRequired(vaccineDetails.getDosesRequired());
        vaccine.setDaysBetweenDoses(vaccineDetails.getDaysBetweenDoses());
        vaccine.setRequired(vaccineDetails.getRequired());
        vaccine.setSideEffects(vaccineDetails.getSideEffects());
        
        return vaccineRepository.save(vaccine);
    }

    @Override
    @Transactional
    public void deleteVaccine(Long id) {
        vaccineRepository.deleteById(id);
    }
}
