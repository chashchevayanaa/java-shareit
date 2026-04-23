package ru.practicum.shareit.request;


import java.util.Optional;

public interface ItemRequestRepository {
    ItemRequest save(ItemRequest request);
    Optional<ItemRequest> findById(Long id);
}