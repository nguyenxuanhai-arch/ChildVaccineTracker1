package com.vaccine.repository;

import com.vaccine.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    
    List<Feedback> findByUserId(Long userId);
    
    @Query("SELECT AVG(f.rating) FROM Feedback f")
    Double calculateAverageRating();
    
    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.rating >= :minRating")
    Integer countFeedbacksWithMinimumRating(Integer minRating);
}
