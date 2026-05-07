package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public BookingResponseDto create(BookingDto bookingDto, Long userId) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Вещь не найдена"));
        if (!item.getAvailable()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Вещь недоступна для бронирования");
        }
        if (item.getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Владелец не может бронировать свою вещь");
        }
        if (bookingDto.getStart() == null || bookingDto.getEnd() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Даты начала и конца обязательны");
        }
        if (bookingDto.getStart().isAfter(bookingDto.getEnd()) || bookingDto.getStart().equals(bookingDto.getEnd())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Некорректные даты бронирования");
        }
        Booking booking = new Booking();
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(Booking.BookingStatus.WAITING);
        booking = bookingRepository.save(booking);
        return bookingMapper.toResponseDto(booking);
    }

    @Override
    @Transactional
    public BookingResponseDto approve(Long bookingId, Long userId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Бронирование не найдено"));
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Подтверждение доступно только владельцу");
        }
        if (!booking.getStatus().equals(Booking.BookingStatus.WAITING)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Бронирование уже обработано");
        }
        booking.setStatus(approved ? Booking.BookingStatus.APPROVED : Booking.BookingStatus.REJECTED);
        booking = bookingRepository.save(booking);
        return bookingMapper.toResponseDto(booking);
    }

    @Override
    public BookingResponseDto getById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Бронирование не найдено"));
        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Доступ запрещён");
        }
        return bookingMapper.toResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getAllByBooker(Long userId, String state) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        List<Booking> bookings;
        try {
            BookingState bookingState = BookingState.valueOf(state.toUpperCase());
            LocalDateTime now = LocalDateTime.now();
            switch (bookingState) {
                case ALL:
                    bookings = bookingRepository.findByBookerId(userId, sort);
                    break;
                case CURRENT:
                    bookings = bookingRepository.findByBookerId(userId, sort).stream()
                            .filter(b -> b.getStart().isBefore(now) && b.getEnd().isAfter(now))
                            .collect(Collectors.toList());
                    break;
                case PAST:
                    bookings = bookingRepository.findByBookerId(userId, sort).stream()
                            .filter(b -> b.getEnd().isBefore(now))
                            .collect(Collectors.toList());
                    break;
                case FUTURE:
                    bookings = bookingRepository.findByBookerId(userId, sort).stream()
                            .filter(b -> b.getStart().isAfter(now))
                            .collect(Collectors.toList());
                    break;
                case WAITING:
                case REJECTED:
                    bookings = bookingRepository.findByBookerIdAndStatus(userId, Booking.BookingStatus.valueOf(state.toUpperCase()), sort);
                    break;
                default:
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown state: " + state);
            }
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown state: " + state);
        }
        return bookings.stream().map(bookingMapper::toResponseDto).collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getAllByOwner(Long userId, String state) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        List<Booking> bookings;
        try {
            BookingState bookingState = BookingState.valueOf(state.toUpperCase());
            LocalDateTime now = LocalDateTime.now();
            switch (bookingState) {
                case ALL:
                    bookings = bookingRepository.findByItemOwnerId(userId, sort);
                    break;
                case CURRENT:
                    bookings = bookingRepository.findByItemOwnerId(userId, sort).stream()
                            .filter(b -> b.getStart().isBefore(now) && b.getEnd().isAfter(now))
                            .collect(Collectors.toList());
                    break;
                case PAST:
                    bookings = bookingRepository.findByItemOwnerId(userId, sort).stream()
                            .filter(b -> b.getEnd().isBefore(now))
                            .collect(Collectors.toList());
                    break;
                case FUTURE:
                    bookings = bookingRepository.findByItemOwnerId(userId, sort).stream()
                            .filter(b -> b.getStart().isAfter(now))
                            .collect(Collectors.toList());
                    break;
                case WAITING:
                case REJECTED:
                    bookings = bookingRepository.findByOwnerIdAndStatus(userId, Booking.BookingStatus.valueOf(state.toUpperCase()), sort);
                    break;
                default:
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown state: " + state);
            }
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown state: " + state);
        }
        return bookings.stream().map(bookingMapper::toResponseDto).collect(Collectors.toList());
    }

    private enum BookingState { ALL, CURRENT, PAST, FUTURE, WAITING, REJECTED }
}