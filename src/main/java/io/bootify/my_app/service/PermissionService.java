package io.bootify.my_app.service;

import io.bootify.my_app.domain.Permission;
import io.bootify.my_app.repos.PermissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(final PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public List<Permission> findAll() {
        return permissionRepository.findAll();
    }

    public List<Permission> findByCategory(String category) {
        return permissionRepository.findByCategory(category);
    }

    public List<Permission> findActive() {
        return permissionRepository.findByActive(true);
    }

    public List<String> findAllCategories() {
        return permissionRepository.findAllCategories();
    }

    public Optional<Permission> findById(Long id) {
        return permissionRepository.findById(id);
    }

    public Optional<Permission> findByName(String name) {
        return permissionRepository.findByName(name);
    }

    public List<Permission> search(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findAll();
        }
        return permissionRepository.searchPermissions(searchTerm.trim());
    }

    public long count() {
        return permissionRepository.count();
    }
}
