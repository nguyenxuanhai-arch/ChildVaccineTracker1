package com.vaccine.service.impl;

import com.vaccine.dto.ServiceDTO;
import com.vaccine.entity.Service;
import com.vaccine.entity.Vaccine;
import com.vaccine.repository.ServiceRepository;
import com.vaccine.repository.VaccineRepository;
import com.vaccine.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class ServiceServiceImpl implements ServiceService {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private VaccineRepository vaccineRepository;

    @Override
    @Transactional
    public Service createService(ServiceDTO serviceDTO) {
        Service service = new Service();
        service.setName(serviceDTO.getName());
        service.setDescription(serviceDTO.getDescription());
        service.setType(serviceDTO.getType());
        service.setPrice(serviceDTO.getPrice());
        service.setActive(serviceDTO.isActive());
        service.setCreatedAt(LocalDateTime.now());
        
        if (serviceDTO.getVaccineIds() != null && !serviceDTO.getVaccineIds().isEmpty()) {
            Set<Vaccine> vaccines = serviceDTO.getVaccineIds().stream()
                    .map(id -> vaccineRepository.findById(id).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            service.setVaccines(vaccines);
        }
        
        return serviceRepository.save(service);
    }

    @Override
    public Optional<Service> findById(Long id) {
        return serviceRepository.findById(id);
    }

    @Override
    public List<Service> findAllServices() {
        return serviceRepository.findAll();
    }

    @Override
    public List<Service> findActiveServices() {
        return serviceRepository.findByActive(true);
    }

    @Override
    public List<Service> findByType(Service.ServiceType type) {
        return serviceRepository.findByType(type);
    }

    @Override
    @Transactional
    public Service updateService(Long id, ServiceDTO serviceDTO) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + id));
        
        service.setName(serviceDTO.getName());
        service.setDescription(serviceDTO.getDescription());
        service.setType(serviceDTO.getType());
        service.setPrice(serviceDTO.getPrice());
        service.setActive(serviceDTO.isActive());
        service.setUpdatedAt(LocalDateTime.now());
        
        if (serviceDTO.getVaccineIds() != null) {
            Set<Vaccine> vaccines = serviceDTO.getVaccineIds().stream()
                    .map(vaccineId -> vaccineRepository.findById(vaccineId).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            service.setVaccines(vaccines);
        }
        
        return serviceRepository.save(service);
    }

    @Override
    @Transactional
    public void deleteService(Long id) {
        serviceRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void toggleServiceStatus(Long id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + id));
        
        service.setActive(!service.isActive());
        service.setUpdatedAt(LocalDateTime.now());
        
        serviceRepository.save(service);
    }
}
