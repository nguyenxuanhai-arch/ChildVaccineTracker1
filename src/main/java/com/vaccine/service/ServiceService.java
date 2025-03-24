package com.vaccine.service;

import com.vaccine.dto.ServiceDTO;
import com.vaccine.entity.Service;

import java.util.List;
import java.util.Optional;

public interface ServiceService {
    
    Service createService(ServiceDTO serviceDTO);
    
    Optional<Service> findById(Long id);
    
    List<Service> findAllServices();
    
    List<Service> findActiveServices();
    
    List<Service> findByType(Service.ServiceType type);
    
    Service updateService(Long id, ServiceDTO serviceDTO);
    
    void deleteService(Long id);
    
    void toggleServiceStatus(Long id);
}
