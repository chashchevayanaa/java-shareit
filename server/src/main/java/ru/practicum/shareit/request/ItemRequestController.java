package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService requestService;

    @PostMapping
    public ItemRequestResponseDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @RequestBody ItemRequestCreateDto dto) {
        return requestService.create(dto, userId);
    }

    @GetMapping
    public List<ItemRequestResponseDto> getOwn(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return requestService.getOwnRequests(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestResponseDto> getAllOther(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return requestService.getAllOtherRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestResponseDto getById(@PathVariable Long requestId) {
        return requestService.getById(requestId);
    }
}
