package io.bootify.my_app.repos;

import io.bootify.my_app.domain.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    
    List<UserProfile> findByUserId(Long userId);
    
    List<UserProfile> findByProfileId(Long profileId);
    
    // Trova i profili attivi per un utente in una determinata data
    @Query("SELECT up FROM UserProfile up WHERE up.user.id = :userId " +
           "AND (up.startDate IS NULL OR up.startDate <= :date) " +
           "AND (up.endDate IS NULL OR up.endDate >= :date)")
    List<UserProfile> findActiveProfilesForUserOnDate(
        @Param("userId") Long userId, 
        @Param("date") LocalDate date);
    
    // Trova i profili attivi per un utente alla data odierna
    default List<UserProfile> findActiveProfilesForUserToday(Long userId) {
        return findActiveProfilesForUserOnDate(userId, LocalDate.now());
    }
}
