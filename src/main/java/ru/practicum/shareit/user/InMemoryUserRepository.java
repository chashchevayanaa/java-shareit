package ru.practicum.shareit.user;


import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryUserRepository implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public User save(User user) {
        if (user.getId() == null) user.setId(idGenerator.getAndIncrement());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User update(User user) {
        if (user.getId() == null || !users.containsKey(user.getId()))
            throw new IllegalArgumentException("Пользователь не найден");
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public boolean existsByEmail(String email) {
        for (User user : users.values()) {
            if (user.getEmail().equals(email)) return true;
        }
        return false;
    }

    @Override
    public void deleteById(Long id) {
        users.remove(id);
    }
}

