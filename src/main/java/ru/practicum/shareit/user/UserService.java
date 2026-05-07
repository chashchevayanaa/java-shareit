package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserDto create(UserDto userDto) {
        log.info("Создание пользователя с email: {}", userDto.getEmail());
        validateUser(userDto);
        if (userRepository.existsByEmail(userDto.getEmail())) {
            log.error("Попытка создания пользователя с уже существующей почтой: {}", userDto.getEmail());
            throw new RuntimeException("Пользователь с почтой " + userDto.getEmail() + " уже существует");
        }
        User user = userMapper.toUser(userDto);
        user.setId(null);
        User saved = userRepository.save(user);
        log.info("Пользователь создан с id={}", saved.getId());
        return userMapper.toDto(saved);
    }

    public UserDto update(Long id, UserDto userDto) {
        log.info("Обновление пользователя id={}", id);
        User existing = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Пользователь с id={} не найден", id);
                    return new RuntimeException("Не найден пользователь с id: " + id);
                });
        if (userDto.getEmail() != null && !userDto.getEmail().equals(existing.getEmail())) {
            if (userRepository.existsByEmail(userDto.getEmail())) {
                log.error("Попытка обновления: email {} уже занят", userDto.getEmail());
                throw new RuntimeException("Email already taken");
            }
        }
        userMapper.updateUser(existing, userDto);
        User updated = userRepository.update(existing);
        log.info("Пользователь id={} обновлён", updated.getId());
        return userMapper.toDto(updated);
    }

    public UserDto findById(Long id) {
        log.debug("Поиск пользователя по id={}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Пользователь с id={} не найден", id);
                    return new RuntimeException("Не найден пользователь с id: " + id);
                });
        return userMapper.toDto(user);
    }

    public List<UserDto> findAll() {
        log.debug("Запрос всех пользователей");
        List<User> users = userRepository.findAll();
        List<UserDto> dtos = new ArrayList<>();
        for (User user : users) {
            dtos.add(userMapper.toDto(user));
        }
        log.debug("Найдено {} пользователей", dtos.size());
        return dtos;
    }

    public void delete(Long id) {
        log.info("Удаление пользователя id={}", id);
        userRepository.deleteById(id);
        log.debug("Пользователь id={} удалён", id);
    }

    private void validateUser(UserDto userDto) {
        if (userDto.getName() == null || userDto.getName().isBlank()) {
            log.warn("Ошибка валидации: имя пользователя пустое");
            throw new RuntimeException("Имя не может быть пустым");
        }
        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            log.warn("Ошибка валидации: email пользователя пустой");
            throw new RuntimeException("Почта не может быть пустой");
        }
        if (!userDto.getEmail().contains("@")) {
            log.warn("Ошибка валидации: email '{}' не содержит @", userDto.getEmail());
            throw new RuntimeException("Почта должна содержать @");
        }
    }
}