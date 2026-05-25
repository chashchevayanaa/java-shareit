package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ItemService itemService;

    @Test
    void createItem() throws Exception {
        ItemDto dto = new ItemDto(1L, "Item", "Desc", true, null);
        when(itemService.create(any(), eq(1L))).thenReturn(dto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ItemDto(null, "Item", "Desc", true, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Item"));
    }

    @Test
    void updateItem() throws Exception {
        when(itemService.update(eq(2L), any(), eq(1L))).thenReturn(new ItemDto(2L, "Upd", "D", false, null));

        mockMvc.perform(patch("/items/2")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"available\":false}"))
                .andExpect(status().isOk());
    }

    @Test
    void getItemById() throws Exception {
        ItemDtoExtended extended = new ItemDtoExtended();
        extended.setId(2L);
        extended.setName("Item");
        when(itemService.findById(2L, 1L)).thenReturn(extended);

        mockMvc.perform(get("/items/2").header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void getOwnerItems() throws Exception {
        when(itemService.findAllByOwnerId(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/items").header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void searchItems() throws Exception {
        when(itemService.search("query")).thenReturn(List.of(new ItemDto(1L, "I", "D", true, null)));

        mockMvc.perform(get("/items/search").param("text", "query"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void addComment() throws Exception {
        when(itemService.addComment(eq(2L), eq(1L), any()))
                .thenReturn(new CommentDto(1L, "Nice", "User", null));

        mockMvc.perform(post("/items/2/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Nice\"}"))
                .andExpect(status().isOk());
        verify(itemService).addComment(eq(2L), eq(1L), any());
    }
}
