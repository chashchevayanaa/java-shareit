package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemRequestRepository requestRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public ItemDto create(ItemDto itemDto, Long ownerId, Long requestId) {
        validateItem(itemDto);
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        ItemRequest request = null;
        if (requestId != null) {
            request = requestRepository.findById(requestId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Запрос не найден"));
        }
        Item item = itemMapper.toItem(itemDto, owner, request);
        item = itemRepository.save(item);
        return itemMapper.toDto(item);
    }

    @Override
    @Transactional
    public ItemDto update(Long itemId, ItemDto itemDto, Long userId) {
        Item existing = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Вещь не найдена"));
        if (!existing.getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Недостаточно прав");
        }
        itemMapper.updateItem(existing, itemDto);
        existing = itemRepository.save(existing);
        return itemMapper.toDto(existing);
    }

    @Override
    public ItemDtoExtended findById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Вещь не найдена"));
        ItemDtoExtended dto = itemMapper.toDtoExtended(item);
        List<Comment> comments = commentRepository.findByItemIdOrderByCreatedDesc(itemId);
        dto.setComments(comments.stream().map(commentMapper::toDto).collect(Collectors.toList()));
        if (item.getOwner().getId().equals(userId)) {
            setBookingsForItem(dto, itemId);
        }
        return dto;
    }

    @Override
    public List<ItemDtoExtended> findAllByOwnerId(Long ownerId) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        List<Item> items = itemRepository.findAllByOwnerId(ownerId);
        List<ItemDtoExtended> dtos = new ArrayList<>();
        for (Item item : items) {
            ItemDtoExtended dto = itemMapper.toDtoExtended(item);
            setBookingsForItem(dto, item.getId());
            List<Comment> comments = commentRepository.findByItemIdOrderByCreatedDesc(item.getId());
            dto.setComments(comments.stream().map(commentMapper::toDto).collect(Collectors.toList()));
            dtos.add(dto);
        }
        return dtos;
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) return Collections.emptyList();
        return itemRepository.searchAvailableByText(text).stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long itemId, Long userId, CommentDto commentDto) {
        if (commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Текст комментария не может быть пустым");
        }
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Вещь не найдена"));
        long completed = bookingRepository.countByBookerIdAndItemIdAndEndBefore(userId, itemId, LocalDateTime.now());
        if (completed == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нельзя оставить комментарий без завершённого бронирования");
        }
        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());
        comment = commentRepository.save(comment);
        return commentMapper.toDto(comment);
    }

    private void validateItem(ItemDto dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Название не может быть пустым");
        }
        if (dto.getDescription() == null || dto.getDescription().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Описание не может быть пустым");
        }
        if (dto.getAvailable() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Статус доступности обязателен");
        }
    }

    private void setBookingsForItem(ItemDtoExtended dto, Long itemId) {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> past = bookingRepository.findPastApprovedBookings(itemId, now);
        if (!past.isEmpty()) {
            Booking last = past.get(0);
            dto.setLastBooking(new BookingDtoShort(last.getId(), last.getBooker().getId()));
        }
        List<Booking> future = bookingRepository.findFutureApprovedBookings(itemId, now);
        if (!future.isEmpty()) {
            Booking next = future.get(0);
            dto.setNextBooking(new BookingDtoShort(next.getId(), next.getBooker().getId()));
        }
    }
}