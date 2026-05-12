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
        log.info("Попытка создания бронирования пользователем id={}, itemId={}", userId, bookingDto.getItemId());
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь id={} не найден", userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден");
                });
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> {
                    log.error("Вещь id={} не найдена", bookingDto.getItemId());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Вещь не найдена");
                });
        if (!item.getAvailable()) {
            log.warn("Вещь id={} недоступна для бронирования", item.getId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Вещь недоступна для бронирования");
        }
        if (item.getOwner().getId().equals(userId)) {
            log.warn("Владелец id={} пытается забронировать свою вещь id={}", userId, item.getId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Владелец не может бронировать свою вещь");
        }
        if (bookingDto.getStart() == null || bookingDto.getEnd() == null) {
            log.warn("Не указаны даты начала или конца бронирования");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Даты начала и конца обязательны");
        }
        if (bookingDto.getStart().isAfter(bookingDto.getEnd()) || bookingDto.getStart().equals(bookingDto.getEnd())) {
            log.warn("Некорректные даты: start={}, end={}", bookingDto.getStart(), bookingDto.getEnd());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Некорректные даты бронирования");
        }

        Booking booking = new Booking();
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(Booking.BookingStatus.WAITING);
        booking = bookingRepository.save(booking);
        log.info("Бронирование создано с id={}", booking.getId());
        return bookingMapper.toResponseDto(booking);
    }

    @Override
    @Transactional
    public BookingResponseDto approve(Long bookingId, Long userId, boolean approved) {
        log.info("Попытка подтверждения бронирования id={} пользователем id={}, approved={}", bookingId, userId, approved);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("Бронирование id={} не найдено", bookingId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Бронирование не найдено");
                });
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            log.warn("Пользователь id={} не является владельцем вещи бронирования id={}", userId, bookingId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Подтверждение доступно только владельцу");
        }
        if (!booking.getStatus().equals(Booking.BookingStatus.WAITING)) {
            log.warn("Бронирование id={} уже обработано, статус={}", bookingId, booking.getStatus());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Бронирование уже обработано");
        }
        booking.setStatus(approved ? Booking.BookingStatus.APPROVED : Booking.BookingStatus.REJECTED);
        booking = bookingRepository.save(booking);
        log.info("Бронирование id={} изменено на статус={}", bookingId, booking.getStatus());
        return bookingMapper.toResponseDto(booking);
    }

    @Override
    public BookingResponseDto getById(Long bookingId, Long userId) {
        log.info("Запрос бронирования id={} пользователем id={}", bookingId, userId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("Бронирование id={} не найдено", bookingId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Бронирование не найдено");
                });
        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            log.warn("Пользователь id={} не имеет доступа к бронированию id={}", userId, bookingId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Доступ запрещён");
        }
        return bookingMapper.toResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getAllByBooker(Long userId, String state) {
        log.info("Получение бронирований для пользователя id={} с состоянием state={}", userId, state);
        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь id={} не найден", userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден");
                });

        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        List<Booking> bookings;
        try {
            BookingState bookingState = BookingState.valueOf(state.toUpperCase());
            bookings = getBookerBookingsByState(userId, bookingState, sort);
        } catch (IllegalArgumentException e) {
            log.error("Некорректный state: {}", state);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown state: " + state);
        }

        return bookings.stream()
                .map(bookingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getAllByOwner(Long userId, String state) {
        log.info("Получение бронирований для владельца id={} с состоянием state={}", userId, state);
        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь id={} не найден", userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден");
                });

        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        List<Booking> bookings;
        try {
            BookingState bookingState = BookingState.valueOf(state.toUpperCase());
            bookings = getOwnerBookingsByState(userId, bookingState, sort);
        } catch (IllegalArgumentException e) {
            log.error("Некорректный state: {}", state);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown state: " + state);
        }

        return bookings.stream()
                .map(bookingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    private List<Booking> getBookerBookingsByState(Long bookerId, BookingState state, Sort sort) {
        LocalDateTime now = LocalDateTime.now();
        switch (state) {
            case ALL:
                log.debug("Запрос ALL бронирований для booker id={}", bookerId);
                return bookingRepository.findByBookerId(bookerId, sort);
            case CURRENT:
                log.debug("Запрос CURRENT бронирований для booker id={}", bookerId);
                return bookingRepository.findCurrentByBookerId(bookerId, now, sort);
            case PAST:
                log.debug("Запрос PAST бронирований для booker id={}", bookerId);
                return bookingRepository.findPastByBookerId(bookerId, now, sort);
            case FUTURE:
                log.debug("Запрос FUTURE бронирований для booker id={}", bookerId);
                return bookingRepository.findFutureByBookerId(bookerId, now, sort);
            case WAITING:
            case REJECTED:
                log.debug("Запрос {} бронирований для booker id={}", state.name(), bookerId);
                Booking.BookingStatus status = Booking.BookingStatus.valueOf(state.name());
                return bookingRepository.findByBookerIdAndStatus(bookerId, status, sort);
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown state: " + state);
        }
    }

    private List<Booking> getOwnerBookingsByState(Long ownerId, BookingState state, Sort sort) {
        LocalDateTime now = LocalDateTime.now();
        switch (state) {
            case ALL:
                log.debug("Запрос ALL бронирований для owner id={}", ownerId);
                return bookingRepository.findByItemOwnerId(ownerId, sort);
            case CURRENT:
                log.debug("Запрос CURRENT бронирований для owner id={}", ownerId);
                return bookingRepository.findCurrentByOwnerId(ownerId, now, sort);
            case PAST:
                log.debug("Запрос PAST бронирований для owner id={}", ownerId);
                return bookingRepository.findPastByOwnerId(ownerId, now, sort);
            case FUTURE:
                log.debug("Запрос FUTURE бронирований для owner id={}", ownerId);
                return bookingRepository.findFutureByOwnerId(ownerId, now, sort);
            case WAITING:
            case REJECTED:
                log.debug("Запрос {} бронирований для owner id={}", state.name(), ownerId);
                Booking.BookingStatus status = Booking.BookingStatus.valueOf(state.name());
                return bookingRepository.findByOwnerIdAndStatus(ownerId, status, sort);
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown state: " + state);
        }
    }
}