package io.bootify.my_app.service;

import io.bootify.my_app.domain.Profile;
import io.bootify.my_app.repos.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileService(final ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public List<Profile> findAll() {
        return profileRepository.findAllWithUsers();
    }

    public List<Profile> findActive() {
        return profileRepository.findByActive(true);
    }

    public Optional<Profile> findById(Long id) {
        return profileRepository.findById(id);
    }

    public Optional<Profile> findByIdWithPermissions(Long id) {
        return profileRepository.findByIdWithPermissions(id);
    }

    public Optional<Profile> findByName(String name) {
        return profileRepository.findByName(name);
    }

    public List<Profile> search(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findAll();
        }
        return profileRepository.searchProfilesWithUsers(searchTerm.trim());
    }

    @Transactional
    public Profile save(Profile profile) {
        return profileRepository.save(profile);
    }

    @Transactional
    public void delete(Long id) {
        profileRepository.deleteById(id);
    }

    public long count() {
        return profileRepository.count();
    }

    public boolean existsByName(String name) {
        return profileRepository.findByName(name).isPresent();
    }

    public boolean existsByNameAndIdNot(String name, Long id) {
        Optional<Profile> existing = profileRepository.findByName(name);
        return existing.isPresent() && !existing.get().getId().equals(id);
    }
}
