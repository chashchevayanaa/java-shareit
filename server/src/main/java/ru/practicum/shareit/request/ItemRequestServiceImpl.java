package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemRequestMapper requestMapper;

    @Override
    @Transactional
    public ItemRequestResponseDto create(ItemRequestCreateDto dto, Long userId) {
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        ItemRequest request = new ItemRequest();
        request.setDescription(dto.getDescription());
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());
        request = requestRepository.save(request);
        return requestMapper.toDtoWithoutItems(request);
    }

    @Override
    public List<ItemRequestResponseDto> getOwnRequests(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        return requestRepository.findAllByRequestorIdOrderByCreatedDesc(userId).stream()
                .map(request -> requestMapper.toDto(request, itemRepository.findAllByRequest_Id(request.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestResponseDto> getAllOtherRequests(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        return requestRepository.findAllByRequestorIdNotOrderByCreatedDesc(userId).stream()
                .map(request -> requestMapper.toDto(request, itemRepository.findAllByRequest_Id(request.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestResponseDto getById(Long requestId) {
        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Запрос не найден"));
        List<Item> items = itemRepository.findAllByRequest_Id(requestId);
        return requestMapper.toDto(request, items);
    }
}
