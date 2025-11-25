package io.bootify.my_app.service;

import io.bootify.my_app.domain.User;
import io.bootify.my_app.repos.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserService(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public List<User> findActive() {
        return userRepository.findByActive(true);
    }

    public List<User> findByProfilesId(Long profileId) {
        return userRepository.findByProfilesId(profileId);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByIdWithProfiles(Long id) {
        return userRepository.findByIdWithProfiles(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> search(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findAll();
        }
        return userRepository.searchUsers(searchTerm.trim());
    }

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    public long count() {
        return userRepository.count();
    }

    public long countActive() {
        return userRepository.countActiveUsers();
    }

    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public boolean existsByUsernameAndIdNot(String username, Long id) {
        Optional<User> existing = userRepository.findByUsername(username);
        return existing.isPresent() && !existing.get().getId().equals(id);
    }

    public boolean existsByEmailAndIdNot(String email, Long id) {
        Optional<User> existing = userRepository.findByEmail(email);
        return existing.isPresent() && !existing.get().getId().equals(id);
    }
}
