package io.bootify.my_app.service;

import io.bootify.my_app.domain.Content;
import io.bootify.my_app.repos.ContentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ContentService {

    private final ContentRepository contentRepository;

    public ContentService(final ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public Page<Content> findAll(Pageable pageable) {
        return contentRepository.findAll(pageable);
    }

    public List<Content> findAll() {
        return contentRepository.findAll();
    }

    public Optional<Content> findById(Long id) {
        return contentRepository.findById(id);
    }

    public Page<Content> search(String searchTerm, Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findAll(pageable);
        }
        return contentRepository.searchContents(searchTerm.trim(), pageable);
    }

    public Page<Content> findByFileType(String fileType, Pageable pageable) {
        return contentRepository.findByFileType(fileType, pageable);
    }

    public Page<Content> findByCategory(String category, Pageable pageable) {
        return contentRepository.findByCategory(category, pageable);
    }

    public Page<Content> searchByFileType(String fileType, String fileName, Pageable pageable) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return findByFileType(fileType, pageable);
        }
        return contentRepository.findByFileTypeAndFileNameContainingIgnoreCase(fileType, fileName.trim(), pageable);
    }

    public Page<Content> searchByCategory(String category, String fileName, Pageable pageable) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return findByCategory(category, pageable);
        }
        return contentRepository.findByCategoryAndFileNameContainingIgnoreCase(category, fileName.trim(), pageable);
    }

    @Transactional
    public Content save(Content content) {
        return contentRepository.save(content);
    }

    @Transactional
    public void delete(Long id) {
        contentRepository.deleteById(id);
    }

    public long count() {
        return contentRepository.count();
    }
}
