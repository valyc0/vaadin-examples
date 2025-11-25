package io.bootify.my_app.repos;

import io.bootify.my_app.domain.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByName(String name);

    List<Permission> findByCategory(String category);

    List<Permission> findByActive(Boolean active);

    @Query("SELECT DISTINCT p.category FROM Permission p ORDER BY p.category")
    List<String> findAllCategories();

    @Query("SELECT p FROM Permission p WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.category) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Permission> searchPermissions(@Param("searchTerm") String searchTerm);
}
