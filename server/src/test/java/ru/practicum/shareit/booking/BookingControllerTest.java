package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.user.UserDto;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private BookingService bookingService;

    @Test
    void createBooking() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(1);
        BookingResponseDto response = new BookingResponseDto(1L, start, end,
                new ItemDto(1L, "Item", "D", true, null),
                new UserDto(2L, "B", "b@test.com"),
                Booking.BookingStatus.WAITING);
        when(bookingService.create(any(), eq(2L))).thenReturn(response);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new BookingDto(1L, start, end))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void approveBooking() throws Exception {
        when(bookingService.approve(1L, 1L, true)).thenReturn(new BookingResponseDto());

        mockMvc.perform(patch("/bookings/1").header("X-Sharer-User-Id", 1L).param("approved", "true"))
                .andExpect(status().isOk());
    }

    @Test
    void getBookingById() throws Exception {
        when(bookingService.getById(1L, 1L)).thenReturn(new BookingResponseDto());

        mockMvc.perform(get("/bookings/1").header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getAllByBooker() throws Exception {
        when(bookingService.getAllByBooker(1L, "ALL")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/bookings").header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getAllByOwner() throws Exception {
        when(bookingService.getAllByOwner(1L, "WAITING")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/bookings/owner").header("X-Sharer-User-Id", 1L).param("state", "WAITING"))
                .andExpect(status().isOk());
    }
}
