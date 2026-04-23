package ru.practicum.shareit.item;


import java.util.List;

public interface ItemService {
    ItemDto create(ItemDto itemDto, Long ownerId, Long requestId);

    ItemDto update(Long itemId, ItemDto itemDto, Long userId);

    ItemDto findById(Long itemId, Long userId);

    List<ItemDto> findAllByOwnerId(Long ownerId);

    List<ItemDto> search(String text);
}