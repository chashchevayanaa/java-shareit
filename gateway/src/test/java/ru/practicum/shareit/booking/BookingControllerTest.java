package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.exception.ErrorHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@Import(ErrorHandler.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private BookingClient bookingClient;

    @Test
    void createBooking_proxiesToClient() throws Exception {
        when(bookingClient.create(eq(1L), any())).thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        LocalDateTime start = LocalDateTime.now().plusDays(2);
        LocalDateTime end = start.plusDays(1);
        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(
                                "{\"itemId\":1,\"start\":\"%s\",\"end\":\"%s\"}", start, end)))
                .andExpect(status().isOk());
    }

    @Test
    void createBooking_invalidDates_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":1,\"start\":\"2020-01-01T10:00:00\",\"end\":\"2020-01-02T10:00:00\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookingById() throws Exception {
        when(bookingClient.getById(1L, 2L)).thenReturn(new ResponseEntity<>(Map.of("id", 2), HttpStatus.OK));

        mockMvc.perform(get("/bookings/2").header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }
}
