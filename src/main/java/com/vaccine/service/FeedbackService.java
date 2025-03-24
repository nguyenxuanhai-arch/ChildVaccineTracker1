package com.vaccine.service;

import com.vaccine.dto.FeedbackDTO;
import com.vaccine.entity.Feedback;
import com.vaccine.entity.User;

import java.util.List;
import java.util.Optional;

public interface FeedbackService {
    
    Feedback createFeedback(FeedbackDTO feedbackDTO, User user);
    
    Optional<Feedback> findById(Long id);
    
    List<Feedback> findByUserId(Long userId);
    
    List<Feedback> findAllFeedbacks();
    
    Feedback updateFeedback(Long id, FeedbackDTO feedbackDTO, User user);
    
    void deleteFeedback(Long id);
    
    double calculateAverageRating();
    
    int countHighRatings(int minRating);
}
