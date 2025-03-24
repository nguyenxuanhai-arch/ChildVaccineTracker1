package com.vaccine.service;

import com.vaccine.dto.ChildDTO;
import com.vaccine.entity.Child;
import com.vaccine.entity.User;

import java.util.List;
import java.util.Optional;

public interface ChildService {
    
    Child createChild(ChildDTO childDTO, User parent);
    
    Optional<Child> findById(Long id);
    
    List<Child> findByParent(User parent);
    
    List<Child> findByParentId(Long parentId);
    
    List<Child> findAllChildren();
    
    Child updateChild(Long id, ChildDTO childDTO);
    
    void deleteChild(Long id);
    
    boolean isParentOfChild(User user, Long childId);
}
