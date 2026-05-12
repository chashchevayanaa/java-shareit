package ru.practicum.shareit.booking;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.UserMapper;

@Component
public class BookingMapper {
    public BookingResponseDto toResponseDto(Booking booking) {
        if (booking == null) return null;
        BookingResponseDto dto = new BookingResponseDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        dto.setStatus(booking.getStatus());
        if (booking.getItem() != null) {
            dto.setItem(new ItemMapper().toDto(booking.getItem()));
        }
        if (booking.getBooker() != null) {
            dto.setBooker(new UserMapper().toDto(booking.getBooker()));
        }
        return dto;
    }
}