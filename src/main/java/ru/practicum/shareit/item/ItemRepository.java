package ru.practicum.shareit.item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Item save(Item item);

    Optional<Item> findById(Long id);

    List<Item> findAllByOwnerId(Long ownerId);

    List<Item> findAll();

    Item update(Item item);

    void deleteById(Long id);

    List<Item> searchAvailableByText(String text);
}