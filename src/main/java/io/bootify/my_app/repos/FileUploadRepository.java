package io.bootify.my_app.repos;

import io.bootify.my_app.domain.FileUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileUploadRepository extends JpaRepository<FileUpload, Long> {
    
    List<FileUpload> findByUploadedByOrderByUploadDateDesc(String uploadedBy);
    
    List<FileUpload> findAllByOrderByUploadDateDesc();
    
    List<FileUpload> findByFileNameContainingIgnoreCaseOrderByUploadDateDesc(String fileName);
}
