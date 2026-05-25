package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void serializeAndDeserializeLocalDateTime() throws Exception {
        LocalDateTime start = LocalDateTime.of(2025, 6, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 6, 2, 10, 0);
        BookingDto dto = new BookingDto(1L, start, end);

        String json = objectMapper.writeValueAsString(dto);
        BookingDto restored = objectMapper.readValue(json, BookingDto.class);

        assertThat(restored.getItemId()).isEqualTo(1L);
        assertThat(restored.getStart()).isEqualTo(start);
        assertThat(restored.getEnd()).isEqualTo(end);
    }
}
