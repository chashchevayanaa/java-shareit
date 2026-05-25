package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceImplIntegrationTest {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(new User(null, "Owner", "owner2@test.com"));
        booker = userRepository.save(new User(null, "Booker", "booker@test.com"));
        item = new Item();
        item.setName("Tent");
        item.setDescription("Camping tent");
        item.setAvailable(true);
        item.setOwner(owner);
        item = itemRepository.save(item);
    }

    @Test
    void getAllByBooker_returnsCreatedBooking() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(1);
        BookingDto bookingDto = new BookingDto(item.getId(), start, end);
        BookingResponseDto created = bookingService.create(bookingDto, booker.getId());

        List<BookingResponseDto> bookings = bookingService.getAllByBooker(booker.getId(), "ALL");

        assertThat(bookings).extracting(BookingResponseDto::getId).contains(created.getId());
    }

    @Test
    void approveBooking_changesStatus() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(1);
        BookingResponseDto created = bookingService.create(
                new BookingDto(item.getId(), start, end), booker.getId());

        BookingResponseDto approved = bookingService.approve(created.getId(), owner.getId(), true);

        assertThat(approved.getStatus()).isEqualTo(Booking.BookingStatus.APPROVED);
    }

    @Test
    void getAllByOwner_waitingState() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        bookingService.create(new BookingDto(item.getId(), start, start.plusDays(1)), booker.getId());

        assertThat(bookingService.getAllByOwner(owner.getId(), "WAITING")).hasSize(1);
    }
}
