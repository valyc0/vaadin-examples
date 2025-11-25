package io.bootify.my_app.repos;

import io.bootify.my_app.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByName(String name);

    List<Profile> findByActive(Boolean active);

    @Query("SELECT p FROM Profile p WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Profile> searchProfiles(@Param("searchTerm") String searchTerm);

    @Query("SELECT p FROM Profile p LEFT JOIN FETCH p.permissions WHERE p.id = :id")
    Optional<Profile> findByIdWithPermissions(@Param("id") Long id);

    @Query("SELECT p FROM Profile p LEFT JOIN FETCH p.users")
    List<Profile> findAllWithUsers();

    @Query("SELECT p FROM Profile p LEFT JOIN FETCH p.users WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Profile> searchProfilesWithUsers(@Param("searchTerm") String searchTerm);
}
