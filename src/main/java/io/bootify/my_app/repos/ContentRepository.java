package io.bootify.my_app.repos;

import io.bootify.my_app.domain.Content;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContentRepository extends JpaRepository<Content, Long> {

    @Query("SELECT c FROM Content c WHERE " +
           "LOWER(c.fileName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.fileType) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.category) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Content> searchContents(@Param("searchTerm") String searchTerm, Pageable pageable);

    Page<Content> findByFileType(String fileType, Pageable pageable);

    List<Content> findByFileType(String fileType);

    Page<Content> findByCategory(String category, Pageable pageable);

    Page<Content> findByFileTypeAndFileNameContainingIgnoreCase(String fileType, String fileName, Pageable pageable);

    Page<Content> findByCategoryAndFileNameContainingIgnoreCase(String category, String fileName, Pageable pageable);
}
