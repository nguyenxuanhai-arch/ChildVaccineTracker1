package com.vaccine.repository;

import com.vaccine.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    
    List<Service> findByActive(boolean active);
    
    List<Service> findByType(Service.ServiceType type);
}
