package imigration.api.service;

import org.springframework.stereotype.Service;

import imigration.api.model.User;
import imigration.api.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void save(final User user) {
        userRepository.save(user);
    }

    public User findByUsername(final String username) {
        return userRepository.findByUsername(username);
    }
}
