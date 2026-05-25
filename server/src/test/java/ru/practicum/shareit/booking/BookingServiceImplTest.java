package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;
    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void setUp() {
        owner = new User(1L, "Owner", "owner@test.com");
        booker = new User(2L, "Booker", "booker@test.com");
        item = new Item();
        item.setId(10L);
        item.setName("Tent");
        item.setAvailable(true);
        item.setOwner(owner);
        start = LocalDateTime.now().plusDays(1);
        end = start.plusDays(2);
        booking = new Booking();
        booking.setId(100L);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(Booking.BookingStatus.WAITING);
    }

    @Test
    void create_success() {
        BookingDto dto = new BookingDto(10L, start, end);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookingMapper.toResponseDto(any())).thenReturn(new BookingResponseDto());

        bookingService.create(dto, 2L);

        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void create_userNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> bookingService.create(new BookingDto(10L, start, end), 2L))
                .isInstanceOf(ResponseStatusException.class)
                .matches(ex -> ((ResponseStatusException) ex).getStatusCode() == HttpStatus.NOT_FOUND);
    }

    @Test
    void create_itemNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(10L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> bookingService.create(new BookingDto(10L, start, end), 2L))
                .matches(ex -> ((ResponseStatusException) ex).getStatusCode() == HttpStatus.NOT_FOUND);
    }

    @Test
    void create_itemUnavailable() {
        item.setAvailable(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        assertThatThrownBy(() -> bookingService.create(new BookingDto(10L, start, end), 2L))
                .matches(ex -> ((ResponseStatusException) ex).getStatusCode() == HttpStatus.BAD_REQUEST);
    }

    @Test
    void create_ownerBooksOwnItem() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        assertThatThrownBy(() -> bookingService.create(new BookingDto(10L, start, end), 1L))
                .matches(ex -> ((ResponseStatusException) ex).getStatusCode() == HttpStatus.BAD_REQUEST);
    }

    @Test
    void create_nullDates() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        assertThatThrownBy(() -> bookingService.create(new BookingDto(10L, null, end), 2L))
                .matches(ex -> ((ResponseStatusException) ex).getStatusCode() == HttpStatus.BAD_REQUEST);
    }

    @Test
    void create_invalidDates() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        LocalDateTime badEnd = start.minusDays(1);
        assertThatThrownBy(() -> bookingService.create(new BookingDto(10L, start, badEnd), 2L))
                .matches(ex -> ((ResponseStatusException) ex).getStatusCode() == HttpStatus.BAD_REQUEST);
    }

    @Test
    void approve_approved() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);
        when(bookingMapper.toResponseDto(any())).thenReturn(new BookingResponseDto());

        bookingService.approve(100L, 1L, true);

        assertThat(booking.getStatus()).isEqualTo(Booking.BookingStatus.APPROVED);
    }

    @Test
    void approve_rejected() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);
        when(bookingMapper.toResponseDto(any())).thenReturn(new BookingResponseDto());

        bookingService.approve(100L, 1L, false);

        assertThat(booking.getStatus()).isEqualTo(Booking.BookingStatus.REJECTED);
    }

    @Test
    void approve_notFound() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> bookingService.approve(100L, 1L, true))
                .matches(ex -> ((ResponseStatusException) ex).getStatusCode() == HttpStatus.NOT_FOUND);
    }

    @Test
    void approve_forbidden() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        assertThatThrownBy(() -> bookingService.approve(100L, 2L, true))
                .matches(ex -> ((ResponseStatusException) ex).getStatusCode() == HttpStatus.FORBIDDEN);
    }

    @Test
    void approve_alreadyProcessed() {
        booking.setStatus(Booking.BookingStatus.APPROVED);
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        assertThatThrownBy(() -> bookingService.approve(100L, 1L, true))
                .matches(ex -> ((ResponseStatusException) ex).getStatusCode() == HttpStatus.BAD_REQUEST);
    }

    @Test
    void getById_asBooker() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(bookingMapper.toResponseDto(booking)).thenReturn(new BookingResponseDto());

        BookingResponseDto result = bookingService.getById(100L, 2L);

        assertThat(result).isNotNull();
    }

    @Test
    void getById_forbidden() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        assertThatThrownBy(() -> bookingService.getById(100L, 99L))
                .matches(ex -> ((ResponseStatusException) ex).getStatusCode() == HttpStatus.FORBIDDEN);
    }

    @Test
    void getAllByBooker_allStates() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerId(eq(2L), any(Sort.class))).thenReturn(List.of(booking));
        when(bookingRepository.findCurrentByBookerId(eq(2L), any(), any())).thenReturn(List.of(booking));
        when(bookingRepository.findPastByBookerId(eq(2L), any(), any())).thenReturn(List.of(booking));
        when(bookingRepository.findFutureByBookerId(eq(2L), any(), any())).thenReturn(List.of(booking));
        when(bookingRepository.findByBookerIdAndStatus(eq(2L), any(), any())).thenReturn(List.of(booking));
        when(bookingMapper.toResponseDto(any())).thenReturn(new BookingResponseDto());

        assertThat(bookingService.getAllByBooker(2L, "ALL")).hasSize(1);
        assertThat(bookingService.getAllByBooker(2L, "CURRENT")).hasSize(1);
        assertThat(bookingService.getAllByBooker(2L, "PAST")).hasSize(1);
        assertThat(bookingService.getAllByBooker(2L, "FUTURE")).hasSize(1);
        assertThat(bookingService.getAllByBooker(2L, "WAITING")).hasSize(1);
        assertThat(bookingService.getAllByBooker(2L, "REJECTED")).hasSize(1);
    }

    @Test
    void getAllByBooker_unknownState() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        assertThatThrownBy(() -> bookingService.getAllByBooker(2L, "INVALID"))
                .matches(ex -> ((ResponseStatusException) ex).getStatusCode() == HttpStatus.BAD_REQUEST);
    }

    @Test
    void getAllByOwner_allStates() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerId(eq(1L), any(Sort.class))).thenReturn(List.of(booking));
        when(bookingRepository.findByOwnerIdAndStatus(eq(1L), any(), any())).thenReturn(List.of(booking));
        when(bookingMapper.toResponseDto(any())).thenReturn(new BookingResponseDto());

        assertThat(bookingService.getAllByOwner(1L, "ALL")).hasSize(1);
        assertThat(bookingService.getAllByOwner(1L, "WAITING")).hasSize(1);
    }

    @Test
    void getAllByOwner_userNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> bookingService.getAllByOwner(1L, "ALL"))
                .matches(ex -> ((ResponseStatusException) ex).getStatusCode() == HttpStatus.NOT_FOUND);
    }

    @Test
    void getAllByBooker_emptyList() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerId(eq(2L), any(Sort.class))).thenReturn(Collections.emptyList());

        assertThat(bookingService.getAllByBooker(2L, "ALL")).isEmpty();
    }
}
