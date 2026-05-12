package ru.practicum.shareit.item;

import java.util.List;

public interface ItemService {
    ItemDto create(ItemDto itemDto, Long ownerId, Long requestId);

    ItemDto update(Long itemId, ItemDto itemDto, Long userId);

    ItemDtoExtended findById(Long itemId, Long userId);

    List<ItemDtoExtended> findAllByOwnerId(Long ownerId);

    List<ItemDto> search(String text);

    CommentDto addComment(Long itemId, Long userId, CommentDto commentDto);
}