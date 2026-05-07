package ru.practicum.shareit.user;


import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);

    Optional<User> findById(Long id);

    List<User> findAll();

    User update(User user);

    public boolean existsByEmail(String email);

    void deleteById(Long id);
}

