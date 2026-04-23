package ru.practicum.shareit.request;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryItemRequestRepository implements ItemRequestRepository {
    private final Map<Long, ItemRequest> requests = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public ItemRequest save(ItemRequest request) {
        if (request.getId() == null) {
            request.setId(idGenerator.getAndIncrement());
        }
        requests.put(request.getId(), request);
        return request;
    }

    @Override
    public Optional<ItemRequest> findById(Long id) {
        return Optional.ofNullable(requests.get(id));
    }
}