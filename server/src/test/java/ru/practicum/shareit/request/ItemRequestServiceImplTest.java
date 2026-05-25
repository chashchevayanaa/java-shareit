package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository requestRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRequestMapper requestMapper;

    @InjectMocks
    private ItemRequestServiceImpl requestService;

    @Test
    void create_userNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> requestService.create(new ItemRequestCreateDto("desc"), 1L))
                .matches(ex -> ((ResponseStatusException) ex).getStatusCode() == HttpStatus.NOT_FOUND);
    }

    @Test
    void getOwnRequests_userNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> requestService.getOwnRequests(1L))
                .matches(ex -> ((ResponseStatusException) ex).getStatusCode() == HttpStatus.NOT_FOUND);
    }

    @Test
    void getAllOtherRequests_success() {
        User user = new User(2L, "U", "u@test.com");
        ItemRequest request = new ItemRequest();
        request.setId(5L);
        request.setDescription("Need bike");
        request.setCreated(LocalDateTime.now());
        Item item = new Item();
        item.setId(10L);
        item.setName("Bike");
        item.setOwner(user);

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(requestRepository.findAllByRequestorIdNotOrderByCreatedDesc(2L)).thenReturn(List.of(request));
        when(itemRepository.findAllByRequest_Id(5L)).thenReturn(List.of(item));
        when(requestMapper.toDto(request, List.of(item))).thenReturn(new ItemRequestResponseDto());

        assertThat(requestService.getAllOtherRequests(2L)).hasSize(1);
    }

    @Test
    void getById_notFound() {
        when(requestRepository.findById(5L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> requestService.getById(5L))
                .matches(ex -> ((ResponseStatusException) ex).getStatusCode() == HttpStatus.NOT_FOUND);
    }

    @Test
    void getById_success() {
        ItemRequest request = new ItemRequest();
        request.setId(5L);
        when(requestRepository.findById(5L)).thenReturn(Optional.of(request));
        when(itemRepository.findAllByRequest_Id(5L)).thenReturn(Collections.emptyList());
        when(requestMapper.toDto(request, Collections.emptyList())).thenReturn(new ItemRequestResponseDto());

        assertThat(requestService.getById(5L)).isNotNull();
    }

    @Test
    void create_success() {
        User user = new User(1L, "U", "u@test.com");
        ItemRequest request = new ItemRequest();
        request.setId(5L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.save(any())).thenReturn(request);
        when(requestMapper.toDtoWithoutItems(request)).thenReturn(new ItemRequestResponseDto());

        assertThat(requestService.create(new ItemRequestCreateDto("Need tent"), 1L)).isNotNull();
    }
}
