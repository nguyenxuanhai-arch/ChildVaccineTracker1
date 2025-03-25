package com.vaccine.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vaccine.dto.FeedbackDTO;
import com.vaccine.entity.Feedback;
import com.vaccine.entity.User;
import com.vaccine.repository.FeedbackRepository;
import com.vaccine.service.FeedbackService;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Override
    @Transactional
    public Feedback createFeedback(FeedbackDTO feedbackDTO, User user) {
        Feedback feedback = new Feedback();
        feedback.setUser(user);
        feedback.setComment(feedbackDTO.getComment());
        feedback.setRating(feedbackDTO.getRating());
        feedback.setCreatedAt(LocalDateTime.now());

        return feedbackRepository.save(feedback);
    }

    @Override
    public Optional<Feedback> findById(Long id) {
        return feedbackRepository.findById(id);
    }

    @Override
    public List<Feedback> findByUserId(Long userId) {
        return feedbackRepository.findByUserId(userId);
    }

    @Override
    public List<Feedback> findAllFeedbacks() {
        return feedbackRepository.findAll();
    }

    @Override
    public double calculateAverageRating() {
        List<Feedback> feedbacks = feedbackRepository.findAll();
        if (feedbacks.isEmpty()) {
            return 0.0;
        }
        return feedbacks.stream()
                .mapToDouble(Feedback::getRating)
                .average()
                .orElse(0.0);
    }

    @Override
    public int countHighRatings(int minRating) {
        List<Feedback> feedbacks = feedbackRepository.findAll();
        return (int) feedbacks.stream()
                .filter(f -> f.getRating() >= minRating)
                .count();
    }

    @Override
    @Transactional
    public Feedback updateFeedback(Long id, FeedbackDTO feedbackDTO, User user) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feedback not found with id: " + id));

        // Check if user is the owner of this feedback
        if (!feedback.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to update this feedback");
        }

        feedback.setComment(feedbackDTO.getComment());
        feedback.setRating(feedbackDTO.getRating());
        feedback.setUpdatedAt(LocalDateTime.now());

        return feedbackRepository.save(feedback);
    }

    @Override
    @Transactional
    public void deleteFeedback(Long id) {
        feedbackRepository.deleteById(id);
    }
}