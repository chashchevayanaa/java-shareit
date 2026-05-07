package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemRequestRepository requestRepository;
    private final ItemMapper itemMapper;

    @Override
    public ItemDto create(ItemDto itemDto, Long ownerId, Long requestId) {
        log.info("Создание товара для пользователя с id={}, requestId={}", ownerId, requestId);
        validateItem(itemDto);
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id={} не найден", ownerId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Не найден пользователь с id: " + ownerId);
                });
        ItemRequest request = null;
        if (requestId != null) {
            request = requestRepository.findById(requestId)
                    .orElseThrow(() -> {
                        log.error("Запрос с id={} не найден", requestId);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Не найден запрос с id: " + requestId);
                    });
        }
        Item item = itemMapper.toItem(itemDto, owner, request);
        item.setId(null);
        Item saved = itemRepository.save(item);
        log.info("Товар создан с id={}", saved.getId());
        return itemMapper.toDto(saved);
    }

    @Override
    public ItemDto update(Long itemId, ItemDto itemDto, Long userId) {
        log.info("Обновление товара id={} пользователем id={}", itemId, userId);
        Item existing = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.error("Товар с id={} не найден", itemId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Не найден товар с id: " + itemId);
                });
        if (existing.getOwner() == null || !existing.getOwner().getId().equals(userId)) {
            log.warn("Пользователь id={} не является владельцем товара id={}", userId, itemId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Пользователь не является владельцем этого товара");
        }
        itemMapper.updateItem(existing, itemDto);
        Item updated = itemRepository.update(existing);
        log.info("Товар id={} обновлён", updated.getId());
        return itemMapper.toDto(updated);
    }

    @Override
    public ItemDto findById(Long itemId, Long userId) {
        log.debug("Поиск товара id={} для пользователя id={}", itemId, userId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.error("Товар с id={} не найден", itemId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Не найден товар с id: " + itemId);
                });
        return itemMapper.toDto(item);
    }

    @Override
    public List<ItemDto> findAllByOwnerId(Long ownerId) {
        log.info("Поиск всех товаров владельца id={}", ownerId);
        userRepository.findById(ownerId)
                .orElseThrow(() -> {
                    log.error("Пользователь id={} не найден", ownerId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден");
                });
        List<Item> items = itemRepository.findAllByOwnerId(ownerId);
        List<ItemDto> dtos = new ArrayList<>();
        for (Item item : items) {
            dtos.add(itemMapper.toDto(item));
        }
        log.debug("Найдено {} товаров для владельца id={}", dtos.size(), ownerId);
        return dtos;
    }

    @Override
    public List<ItemDto> search(String text) {
        log.debug("Поиск товаров по тексту: '{}'", text);
        if (text == null || text.isBlank()) {
            log.debug("Поисковый запрос пуст, возвращаем пустой список");
            return new ArrayList<>();
        }
        List<Item> items = itemRepository.searchAvailableByText(text);
        List<ItemDto> dtos = new ArrayList<>();
        for (Item item : items) {
            dtos.add(itemMapper.toDto(item));
        }
        log.debug("Найдено {} товаров по запросу '{}'", dtos.size(), text);
        return dtos;
    }

    private void validateItem(ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            log.warn("Ошибка валидации: название товара пустое");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Названиие товара не может быть пустым");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            log.warn("Ошибка валидации: описание товара пустое");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Описание товара не можнт быть пустым");
        }
        if (itemDto.getAvailable() == null) {
            log.warn("Ошибка валидации: статус доступности товара не указан");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Необходимо указать статус товара");
        }
    }
}

