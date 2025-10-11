package io.bootify.my_app.service;

import io.bootify.my_app.domain.FileUpload;
import io.bootify.my_app.repos.FileUploadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FileUploadService {

    private final FileUploadRepository fileUploadRepository;

    public FileUploadService(FileUploadRepository fileUploadRepository) {
        this.fileUploadRepository = fileUploadRepository;
    }

    public List<FileUpload> findAll() {
        return fileUploadRepository.findAllByOrderByUploadDateDesc();
    }

    public Optional<FileUpload> findById(Long id) {
        return fileUploadRepository.findById(id);
    }

    public List<FileUpload> findByUploadedBy(String uploadedBy) {
        return fileUploadRepository.findByUploadedByOrderByUploadDateDesc(uploadedBy);
    }

    public List<FileUpload> searchByFileName(String fileName) {
        return fileUploadRepository.findByFileNameContainingIgnoreCaseOrderByUploadDateDesc(fileName);
    }

    public List<FileUpload> findByCategory(String category) {
        return fileUploadRepository.findByCategoryOrderByUploadDateDesc(category);
    }

    public List<FileUpload> findByStatus(String status) {
        return fileUploadRepository.findByStatusOrderByUploadDateDesc(status);
    }

    public FileUpload save(FileUpload fileUpload) {
        return fileUploadRepository.save(fileUpload);
    }

    public FileUpload updateStatus(Long id, String status) {
        Optional<FileUpload> fileOpt = fileUploadRepository.findById(id);
        if (fileOpt.isPresent()) {
            FileUpload file = fileOpt.get();
            file.setStatus(status);
            return fileUploadRepository.save(file);
        }
        return null;
    }

    public void delete(Long id) {
        fileUploadRepository.deleteById(id);
    }

    public void deleteAll(List<FileUpload> files) {
        fileUploadRepository.deleteAll(files);
    }

    public long count() {
        return fileUploadRepository.count();
    }
}
