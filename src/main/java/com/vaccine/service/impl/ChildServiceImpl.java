package com.vaccine.service.impl;

import com.vaccine.dto.ChildDTO;
import com.vaccine.entity.Child;
import com.vaccine.entity.User;
import com.vaccine.repository.ChildRepository;
import com.vaccine.service.ChildService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ChildServiceImpl implements ChildService {

    @Autowired
    private ChildRepository childRepository;

    @Override
    @Transactional
    public Child createChild(ChildDTO childDTO, User parent) {
        Child child = new Child();
        child.setFullName(childDTO.getFullName());
        child.setDateOfBirth(childDTO.getDateOfBirth());
        child.setGender(childDTO.getGender());
        child.setBloodType(childDTO.getBloodType());
        child.setWeight(childDTO.getWeight());
        child.setHeight(childDTO.getHeight());
        child.setAllergies(childDTO.getAllergies());
        child.setMedicalConditions(childDTO.getMedicalConditions());
        child.setParent(parent);
        child.setCreatedAt(LocalDateTime.now());
        
        return childRepository.save(child);
    }

    @Override
    public Optional<Child> findById(Long id) {
        return childRepository.findById(id);
    }

    @Override
    public List<Child> findByParent(User parent) {
        return childRepository.findByParent(parent);
    }

    @Override
    public List<Child> findByParentId(Long parentId) {
        return childRepository.findByParentId(parentId);
    }

    @Override
    public List<Child> findAllChildren() {
        return childRepository.findAll();
    }

    @Override
    @Transactional
    public Child updateChild(Long id, ChildDTO childDTO) {
        Child child = childRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Child not found with id: " + id));
        
        child.setFullName(childDTO.getFullName());
        child.setDateOfBirth(childDTO.getDateOfBirth());
        child.setGender(childDTO.getGender());
        child.setBloodType(childDTO.getBloodType());
        child.setWeight(childDTO.getWeight());
        child.setHeight(childDTO.getHeight());
        child.setAllergies(childDTO.getAllergies());
        child.setMedicalConditions(childDTO.getMedicalConditions());
        child.setUpdatedAt(LocalDateTime.now());
        
        return childRepository.save(child);
    }

    @Override
    @Transactional
    public void deleteChild(Long id) {
        childRepository.deleteById(id);
    }

    @Override
    public boolean isParentOfChild(User user, Long childId) {
        Optional<Child> child = childRepository.findById(childId);
        return child.isPresent() && child.get().getParent().getId().equals(user.getId());
    }
}
