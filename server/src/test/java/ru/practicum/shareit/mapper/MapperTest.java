package ru.practicum.shareit.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.Comment;
import ru.practicum.shareit.item.CommentDto;
import ru.practicum.shareit.item.CommentMapper;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.item.ItemDtoExtended;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MapperTest {

    private final UserMapper userMapper = new UserMapper();
    private final ItemMapper itemMapper = new ItemMapper();
    private final BookingMapper bookingMapper = new BookingMapper();
    private final CommentMapper commentMapper = new CommentMapper();
    private final ItemRequestMapper requestMapper = new ItemRequestMapper();

    @Test
    void userMapper_nullAndRoundTrip() {
        assertThat(userMapper.toDto(null)).isNull();
        assertThat(userMapper.toUser(null)).isNull();
        User user = new User(1L, "Name", "e@test.com");
        UserDto dto = userMapper.toDto(user);
        assertThat(dto.getName()).isEqualTo("Name");
        User mapped = userMapper.toUser(dto);
        userMapper.updateUser(mapped, new UserDto(null, "New", null));
        assertThat(mapped.getName()).isEqualTo("New");
    }

    @Test
    void itemMapper_allMethods() {
        assertThat(itemMapper.toDto(null)).isNull();
        assertThat(itemMapper.toItem(null, null, null)).isNull();
        assertThat(itemMapper.toDtoExtended(null)).isNull();

        User owner = new User(1L, "O", "o@test.com");
        Item item = new Item();
        item.setId(1L);
        item.setName("N");
        item.setDescription("D");
        item.setAvailable(true);
        item.setOwner(owner);

        ItemDto dto = itemMapper.toDto(item);
        assertThat(dto.getName()).isEqualTo("N");

        Item mapped = itemMapper.toItem(dto, owner, null);
        itemMapper.updateItem(mapped, new ItemDto(null, "Upd", "Desc2", false, null));
        assertThat(mapped.getName()).isEqualTo("Upd");

        ItemDtoExtended extended = itemMapper.toDtoExtended(item);
        assertThat(extended.getId()).isEqualTo(1L);
    }

    @Test
    void bookingMapper_withItemAndBooker() {
        assertThat(bookingMapper.toResponseDto(null)).isNull();

        User owner = new User(1L, "O", "o@test.com");
        User booker = new User(2L, "B", "b@test.com");
        Item item = new Item();
        item.setId(10L);
        item.setName("Item");
        item.setDescription("D");
        item.setAvailable(true);
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setId(100L);
        booking.setStart(LocalDateTime.now());
        booking.setEnd(LocalDateTime.now().plusDays(1));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(Booking.BookingStatus.WAITING);

        BookingResponseDto dto = bookingMapper.toResponseDto(booking);
        assertThat(dto.getItem().getName()).isEqualTo("Item");
        assertThat(dto.getBooker().getId()).isEqualTo(2L);
    }

    @Test
    void commentMapper_nullAndMapping() {
        assertThat(commentMapper.toDto(null)).isNull();

        User author = new User(1L, "Author", "a@test.com");
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Nice");
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        CommentDto dto = commentMapper.toDto(comment);
        assertThat(dto.getAuthorName()).isEqualTo("Author");
    }

    @Test
    void itemRequestMapper() {
        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Need");
        request.setCreated(LocalDateTime.now());

        ItemRequestResponseDto without = requestMapper.toDtoWithoutItems(request);
        assertThat(without.getItems()).isEmpty();

        Item item = new Item();
        item.setId(10L);
        item.setName("Bike");
        item.setOwner(new User(2L, "O", "o@test.com"));

        ItemRequestResponseDto with = requestMapper.toDto(request, List.of(item));
        assertThat(with.getItems()).hasSize(1);
        assertThat(with.getItems().get(0).getOwnerId()).isEqualTo(2L);
    }
}
