package ru.practicum.shareit.request;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.request.dto.ItemRequestItemDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ItemRequestMapper {

    public ItemRequestResponseDto toDto(ItemRequest request, List<Item> items) {
        ItemRequestResponseDto dto = new ItemRequestResponseDto();
        dto.setId(request.getId());
        dto.setDescription(request.getDescription());
        dto.setCreated(request.getCreated());
        dto.setItems(items.stream().map(this::toItemDto).collect(Collectors.toList()));
        return dto;
    }

    public ItemRequestResponseDto toDtoWithoutItems(ItemRequest request) {
        ItemRequestResponseDto dto = new ItemRequestResponseDto();
        dto.setId(request.getId());
        dto.setDescription(request.getDescription());
        dto.setCreated(request.getCreated());
        dto.setItems(new ArrayList<>());
        return dto;
    }

    private ItemRequestItemDto toItemDto(Item item) {
        return new ItemRequestItemDto(item.getId(), item.getName(), item.getOwner().getId());
    }
}
