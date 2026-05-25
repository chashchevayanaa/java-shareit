package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void create_success() {
        UserDto dto = new UserDto(null, "Test", "test@test.com");
        User user = new User(1L, "Test", "test@test.com");
        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(userMapper.toUser(dto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(new UserDto(1L, "Test", "test@test.com"));

        UserDto result = userService.create(dto);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void create_duplicateEmail() {
        UserDto dto = new UserDto(null, "Test", "test@test.com");
        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(dto))
                .matches(ex -> ((ResponseStatusException) ex).getStatusCode() == HttpStatus.CONFLICT);
    }

    @Test
    void update_success() {
        User existing = new User(1L, "Old", "old@test.com");
        UserDto dto = new UserDto(null, "New", "new@test.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(userRepository.save(existing)).thenReturn(existing);
        when(userMapper.toDto(existing)).thenReturn(new UserDto(1L, "New", "new@test.com"));

        UserDto result = userService.update(1L, dto);

        assertThat(result.getName()).isEqualTo("New");
        verify(userMapper).updateUser(existing, dto);
    }

    @Test
    void update_sameEmail() {
        User existing = new User(1L, "Old", "old@test.com");
        UserDto dto = new UserDto(null, "New", "old@test.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);
        when(userMapper.toDto(existing)).thenReturn(new UserDto(1L, "New", "old@test.com"));

        userService.update(1L, dto);
    }

    @Test
    void update_emailConflict() {
        User existing = new User(1L, "Old", "old@test.com");
        UserDto dto = new UserDto(null, "New", "taken@test.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.update(1L, dto))
                .matches(ex -> ((ResponseStatusException) ex).getStatusCode() == HttpStatus.CONFLICT);
    }

    @Test
    void update_notFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.update(1L, new UserDto()))
                .matches(ex -> ((ResponseStatusException) ex).getStatusCode() == HttpStatus.NOT_FOUND);
    }

    @Test
    void findById_success() {
        User user = new User(1L, "Test", "test@test.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(new UserDto(1L, "Test", "test@test.com"));

        assertThat(userService.findById(1L).getEmail()).isEqualTo("test@test.com");
    }

    @Test
    void findById_notFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.findById(1L))
                .matches(ex -> ((ResponseStatusException) ex).getStatusCode() == HttpStatus.NOT_FOUND);
    }

    @Test
    void findAll() {
        User user = new User(1L, "Test", "test@test.com");
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toDto(user)).thenReturn(new UserDto(1L, "Test", "test@test.com"));

        assertThat(userService.findAll()).hasSize(1);
    }

    @Test
    void delete() {
        userService.delete(1L);
        verify(userRepository).deleteById(1L);
    }
}
