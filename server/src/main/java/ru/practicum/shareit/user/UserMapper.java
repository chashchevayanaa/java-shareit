package ru.practicum.shareit.user;

import org.springframework.stereotype.Component;

@Component
public class UserMapper {


    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());    // ВНИМАНИЕ: должно быть user.getName()
        dto.setEmail(user.getEmail());  // ВНИМАНИЕ: должно быть user.getEmail()
        return dto;
    }

    public User toUser(UserDto dto) {
        if (dto == null) {
            return null;
        }
        User user = new User();
        user.setId(dto.getId());
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        return user;
    }

    public void updateUser(User existing, UserDto dto) {
        if (dto.getName() != null) {
            existing.setName(dto.getName());
        }
        if (dto.getEmail() != null) {
            existing.setEmail(dto.getEmail());
        }
    }
}
