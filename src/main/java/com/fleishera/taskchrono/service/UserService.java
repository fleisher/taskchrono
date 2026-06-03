package com.fleishera.taskchrono.service;

import com.fleishera.taskchrono.entity.User;
import com.fleishera.taskchrono.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User getOrCreateUser(Long telegramId, String username) {
        return userRepository.findByTelegramId(telegramId)
                .map(user -> {
                    if (username != null && !username.equals(user.getUsername())) {
                        user.setUsername(username);
                        return userRepository.save(user);
                    }
                    return user;
                })
                .orElseGet(() -> userRepository.save(User.builder()
                        .telegramId(telegramId)
                        .username(username)
                        .build()));
    }
    @Transactional
    public void updateUserState(User user, String state) {
        user.setBotState(state);
        userRepository.save(user);
    }
}
