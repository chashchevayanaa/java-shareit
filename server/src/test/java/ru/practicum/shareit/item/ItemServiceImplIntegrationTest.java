package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemService itemService;
    @Autowired
    private UserRepository userRepository;

    private User owner;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(new User(null, "Owner", "owner@test.com"));
    }

    @Test
    void findAllByOwnerId_returnsOwnerItems() {
        ItemDto itemDto = new ItemDto(null, "Drill", "Power drill", true, null);
        itemService.create(itemDto, owner.getId());

        List<ItemDtoExtended> items = itemService.findAllByOwnerId(owner.getId());

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getName()).isEqualTo("Drill");
    }
}
