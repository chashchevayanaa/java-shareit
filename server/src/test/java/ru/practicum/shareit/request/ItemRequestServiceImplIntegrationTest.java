package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemRequestServiceImplIntegrationTest {

    @Autowired
    private ItemRequestService requestService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemService itemService;

    private User requestor;

    @BeforeEach
    void setUp() {
        requestor = userRepository.save(new User(null, "Requestor", "requestor@test.com"));
    }

    @Test
    void createAndGetOwnRequests() {
        ItemRequestCreateDto dto = new ItemRequestCreateDto("Need a bike");
        ItemRequestResponseDto created = requestService.create(dto, requestor.getId());

        List<ItemRequestResponseDto> own = requestService.getOwnRequests(requestor.getId());

        assertThat(created.getId()).isNotNull();
        assertThat(created.getDescription()).isEqualTo("Need a bike");
        assertThat(own).hasSize(1);
        assertThat(own.get(0).getId()).isEqualTo(created.getId());
    }

    @Test
    void getAllOtherRequests_excludesOwn() {
        User other = userRepository.save(new User(null, "Other", "other@test.com"));
        requestService.create(new ItemRequestCreateDto("Need tent"), requestor.getId());
        requestService.create(new ItemRequestCreateDto("Need bike"), other.getId());

        assertThat(requestService.getAllOtherRequests(requestor.getId())).hasSize(1);
    }

    @Test
    void getById_returnsItems() {
        ItemRequestResponseDto created = requestService.create(
                new ItemRequestCreateDto("Need drill"), requestor.getId());
        User responder = userRepository.save(new User(null, "Resp", "resp@test.com"));
        itemService.create(new ItemDto(null, "Drill", "Power", true, created.getId()), responder.getId());

        ItemRequestResponseDto found = requestService.getById(created.getId());

        assertThat(found.getItems()).hasSize(1);
    }
}
