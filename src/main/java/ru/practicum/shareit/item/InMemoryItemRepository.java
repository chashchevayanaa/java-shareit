package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryItemRepository implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(idGenerator.getAndIncrement());
        }
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public List<Item> findAllByOwnerId(Long ownerId) {
        List<Item> resalt = new ArrayList<>();
        for (Item item : items.values()) {
            if (item.getOwner() != null && item.getOwner().getId().equals(ownerId)) {
                resalt.add(item);
            }
        }
        return resalt;
    }

    @Override
    public List<Item> findAll() {
        return new ArrayList<>(items.values());
    }

    @Override
    public Item update(Item item) {
        if (item.getId() == null || !items.containsKey(item.getId())) {
            throw new RuntimeException("Вещь не найдена");
        }
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public void deleteById(Long id) {
        items.remove(id);
    }

    @Override
    public List<Item> searchAvailableByText(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        String lowerText = text.toLowerCase();
        List<Item> result = new ArrayList<>();
        for (Item item : items.values()) {
            if (item.getAvailable() != null && item.getAvailable()) {
                String name = item.getName();
                String description = item.getDescription();
                if ((name != null && name.toLowerCase().contains(lowerText)) ||
                        (description != null && description.toLowerCase().contains(lowerText))) {
                    result.add(item);
                }
            }
        }
        return result;
    }
}
