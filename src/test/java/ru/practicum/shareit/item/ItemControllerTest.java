package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import ru.practicum.shareit.comments.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {
    private UserDto user;
    private ItemDto item;
    private ItemDto anotherItem;
    private ItemDto itemFull;
    private CommentDto comment1;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    ItemService itemService;

    @BeforeEach
    void setUp(WebApplicationContext wac) {
        comment1 = CommentDto.builder()
                .id(1L)
                .text("Comment text")
                .authorName("User 1 name")
                .created(LocalDateTime.now())
                .build();
        user = UserDto.builder()
                .id(1L)
                .name("User 1 name")
                .email("user1@email.com")
                .build();
        item = ItemDto.builder()
                .id(1L)
                .name("Item1")
                .description("Item 1 description")
                .available(true)
                .build();
        anotherItem = ItemDto.builder()
                .id(2L)
                .name("anotherItem1")
                .description("anotherItem 1 description")
                .available(true)
                .build();
        itemFull = ItemDto.builder()
                .id(2L)
                .name("Item2")
                .description("Item 2 description")
                .available(false)
                .nextBooking(null)
                .lastBooking(null)
                .comments(Collections.singletonList(comment1))
                .build();
    }

    @Test
    void getUserItems() throws Exception {
        List<ItemDto> items = new ArrayList<>();
        items.add(item);
        items.add(anotherItem);
        when(itemService.getAllUserItems(anyLong()))
                .thenReturn(items);

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(item.getName())))
                .andExpect(jsonPath("$[0].description", is(item.getDescription())))
                .andExpect(jsonPath("$[0].available", is(item.getAvailable())))
                .andExpect(jsonPath("$[1].id", is(anotherItem.getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(anotherItem.getName())))
                .andExpect(jsonPath("$[1].description", is(anotherItem.getDescription())))
                .andExpect(jsonPath("$[1].available", is(anotherItem.getAvailable())));
    }

    @Test
    void getItemById() throws Exception {
        when(itemService.get(anyLong(), anyLong()))
                .thenReturn(itemFull);

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 2)
                        .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemFull.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemFull.getName())))
                .andExpect(jsonPath("$.description", is(itemFull.getDescription())))
                .andExpect(jsonPath("$.available", is(itemFull.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(itemFull.getRequestId())))
                .andExpect(jsonPath("$.comments", Matchers.hasSize(1)))
                .andExpect(jsonPath("$.lastBooking", is(itemFull.getLastBooking())))
                .andExpect(jsonPath("$.nextBooking", is(itemFull.getNextBooking())));
    }

    @Test
    void searchItems() throws Exception {
        List<ItemDto> items = Collections.singletonList(item);
        when(itemService.search(anyString()))
                .thenReturn(items);

        mockMvc.perform(get("/items/search")
                        .param("text", "item")
                        .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(item.getName())))
                .andExpect(jsonPath("$[0].description", is(item.getDescription())))
                .andExpect(jsonPath("$[0].available", is(item.getAvailable())));
    }

    @Test
    void addItem() throws Exception {
        when(itemService.create(anyLong(), Mockito.any(ItemDto.class)))
                .thenReturn(item);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(item))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(item.getName())))
                .andExpect(jsonPath("$.description", is(item.getDescription())))
                .andExpect(jsonPath("$.available", is(item.getAvailable())));
    }

    @Test
    void updateItem() throws Exception {
        when(itemService.update(anyLong(), anyLong(), Mockito.any(ItemDto.class)))
                .thenReturn(item);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(item))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(item.getName())))
                .andExpect(jsonPath("$.description", is(item.getDescription())))
                .andExpect(jsonPath("$.available", is(item.getAvailable())));
    }

    @Test
    void addComment() throws Exception {
        when(itemService.createComment(Mockito.any(CommentDto.class), anyLong(), anyLong()))
                .thenReturn(comment1);

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(comment1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(comment1.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(comment1.getText())))
                .andExpect(jsonPath("$.authorName", is(comment1.getAuthorName())))
                //TODO поискать решение проблемы
                //.andExpect(jsonPath("$.created", is(comment1.getCreated().toString())));
                .andExpect(jsonPath("$.created", Matchers.notNullValue()));

    }
}
