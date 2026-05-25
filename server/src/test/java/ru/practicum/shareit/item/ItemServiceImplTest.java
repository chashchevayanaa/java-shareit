package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
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
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRequestRepository requestRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private Item item;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        owner = new User(1L, "Owner", "owner@test.com");
        item = new Item();
        item.setId(10L);
        item.setName("Tent");
        item.setDescription("Camping");
        item.setAvailable(true);
        item.setOwner(owner);
        itemDto = new ItemDto(10L, "Tent", "Camping", true, null);
    }

    @Test
    void create_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemMapper.toItem(itemDto, owner, null)).thenReturn(item);
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(itemDto);

        ItemDto result = itemService.create(itemDto, 1L);

        assertThat(result).isEqualTo(itemDto);
    }

    @Test
    void create_withRequestId() {
        ItemRequest request = new ItemRequest();
        request.setId(5L);
        itemDto.setRequestId(5L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(requestRepository.findById(5L)).thenReturn(Optional.of(request));
        when(itemMapper.toItem(itemDto, owner, request)).thenReturn(item);
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(itemDto);

        itemService.create(itemDto, 1L);

        verify(requestRepository).findById(5L);
    }

    @Test
    void create_userNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> itemService.create(itemDto, 1L))
                .matches(ex -> ((ResponseStatusException) ex).getStatusCode() == HttpStatus.NOT_FOUND);
    }

    @Test
    void create_requestNotFound() {
        itemDto.setRequestId(99L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(requestRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> itemService.create(itemDto, 1L))
                .matches(ex -> ((ResponseStatusException) ex).getStatusCode() == HttpStatus.NOT_FOUND);
    }

    @Test
    void update_success() {
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(itemDto);

        itemService.update(10L, itemDto, 1L);

        verify(itemMapper).updateItem(item, itemDto);
    }

    @Test
    void update_forbidden() {
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        assertThatThrownBy(() -> itemService.update(10L, itemDto, 2L))
                .matches(ex -> ((ResponseStatusException) ex).getStatusCode() == HttpStatus.FORBIDDEN);
    }

    @Test
    void findById_asOwnerWithBookings() {
        ItemDtoExtended extended = new ItemDtoExtended();
        Booking past = new Booking();
        past.setId(1L);
        past.setBooker(new User(2L, "B", "b@test.com"));
        Booking future = new Booking();
        future.setId(2L);
        future.setBooker(new User(3L, "C", "c@test.com"));

        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(itemMapper.toDtoExtended(item)).thenReturn(extended);
        when(commentRepository.findByItemIdOrderByCreatedDesc(10L)).thenReturn(Collections.emptyList());
        when(bookingRepository.findPastApprovedBookings(eq(10L), any())).thenReturn(List.of(past));
        when(bookingRepository.findFutureApprovedBookings(eq(10L), any())).thenReturn(List.of(future));

        ItemDtoExtended result = itemService.findById(10L, 1L);

        assertThat(result.getLastBooking()).isNotNull();
        assertThat(result.getNextBooking()).isNotNull();
    }

    @Test
    void findById_asOtherUser() {
        ItemDtoExtended extended = new ItemDtoExtended();
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(itemMapper.toDtoExtended(item)).thenReturn(extended);
        when(commentRepository.findByItemIdOrderByCreatedDesc(10L)).thenReturn(Collections.emptyList());

        ItemDtoExtended result = itemService.findById(10L, 2L);

        assertThat(result.getLastBooking()).isNull();
    }

    @Test
    void findAllByOwnerId() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findAllByOwnerId(1L)).thenReturn(List.of(item));
        when(itemMapper.toDtoExtended(item)).thenReturn(new ItemDtoExtended());
        when(commentRepository.findByItemIdOrderByCreatedDesc(10L)).thenReturn(Collections.emptyList());
        when(bookingRepository.findPastApprovedBookings(eq(10L), any())).thenReturn(Collections.emptyList());
        when(bookingRepository.findFutureApprovedBookings(eq(10L), any())).thenReturn(Collections.emptyList());

        assertThat(itemService.findAllByOwnerId(1L)).hasSize(1);
    }

    @Test
    void search_blankReturnsEmpty() {
        assertThat(itemService.search(null)).isEmpty();
        assertThat(itemService.search("   ")).isEmpty();
    }

    @Test
    void search_withText() {
        when(itemRepository.searchAvailableByText("tent")).thenReturn(List.of(item));
        when(itemMapper.toDto(item)).thenReturn(itemDto);

        assertThat(itemService.search("tent")).hasSize(1);
    }

    @Test
    void addComment_success() {
        CommentDto commentDto = new CommentDto(null, "Great", null, null);
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Great");

        when(userRepository.findById(2L)).thenReturn(Optional.of(new User(2L, "B", "b@test.com")));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(bookingRepository.countByBookerIdAndItemIdAndEndBefore(eq(2L), eq(10L), any())).thenReturn(1L);
        when(commentRepository.save(any())).thenReturn(comment);
        when(commentMapper.toDto(comment)).thenReturn(commentDto);

        assertThat(itemService.addComment(10L, 2L, commentDto).getText()).isEqualTo("Great");
    }

    @Test
    void addComment_emptyText() {
        assertThatThrownBy(() -> itemService.addComment(10L, 2L, new CommentDto(null, " ", null, null)))
                .matches(ex -> ((ResponseStatusException) ex).getStatusCode() == HttpStatus.BAD_REQUEST);
    }

    @Test
    void addComment_noCompletedBooking() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(new User(2L, "B", "b@test.com")));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(bookingRepository.countByBookerIdAndItemIdAndEndBefore(eq(2L), eq(10L), any())).thenReturn(0L);

        assertThatThrownBy(() -> itemService.addComment(10L, 2L, new CommentDto(null, "Hi", null, null)))
                .matches(ex -> ((ResponseStatusException) ex).getStatusCode() == HttpStatus.BAD_REQUEST);
    }
}
