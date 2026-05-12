package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

public interface BookingService {
    BookingResponseDto create(BookingDto bookingDto, Long userId);

    BookingResponseDto approve(Long bookingId, Long userId, boolean approved);

    BookingResponseDto getById(Long bookingId, Long userId);

    List<BookingResponseDto> getAllByBooker(Long userId, String state);

    List<BookingResponseDto> getAllByOwner(Long userId, String state);
}