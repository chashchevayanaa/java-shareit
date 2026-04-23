package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserDto create(UserDto userDto) {
        validateUser(userDto);
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new RuntimeException("Пользователь с почтой " + userDto.getEmail() + " уже существует");
        }
        User user = userMapper.toUser(userDto);
        user.setId(null);
        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    public UserDto update(Long id, UserDto userDto) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Не найден пользователь с id: " + id));
        if (userDto.getEmail() != null && !userDto.getEmail().equals(existing.getEmail())) {
            if (userRepository.existsByEmail(userDto.getEmail())) {
                throw new RuntimeException("Email already taken");
            }
        }
        userMapper.updateUser(existing, userDto);
        User updated = userRepository.update(existing);
        return userMapper.toDto(updated);
    }

    public UserDto findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Не найден пользователь с id: " + id));
        return userMapper.toDto(user);
    }

    public List<UserDto> findAll() {
        List<User> users = userRepository.findAll();
        List<UserDto> dtos = new ArrayList<>();
        for (User user : users) {
            dtos.add(userMapper.toDto(user));
        }
        return dtos;
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    private void validateUser(UserDto userDto) {
        if (userDto.getName() == null || userDto.getName().isBlank()) {
            throw new RuntimeException("Имя не может быть пустым");
        }
        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            throw new RuntimeException("Почта не может быть пустой");
        }
        if (!userDto.getEmail().contains("@")) {
            throw new RuntimeException("Почта должна содержать @");
        }
    }
}