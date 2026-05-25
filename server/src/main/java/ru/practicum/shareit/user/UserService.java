package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserDto create(UserDto userDto) {
        log.info("Создание пользователя с email: {}", userDto.getEmail());
        if (userRepository.existsByEmail(userDto.getEmail())) {
            log.error("Email уже используется: {}", userDto.getEmail());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Пользователь с такой почтой уже существует");
        }
        User user = userMapper.toUser(userDto);
        user = userRepository.save(user);
        log.info("Пользователь создан с id={}", user.getId());
        return userMapper.toDto(user);
    }

    public UserDto update(Long id, UserDto userDto) {
        log.info("Обновление пользователя id={}", id);
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        if (userDto.getEmail() != null && !userDto.getEmail().equals(existing.getEmail())) {
            if (userRepository.existsByEmail(userDto.getEmail())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email уже занят");
            }
        }
        userMapper.updateUser(existing, userDto);
        existing = userRepository.save(existing);
        return userMapper.toDto(existing);
    }

    public UserDto findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        return userMapper.toDto(user);
    }

    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }

}