package java.ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDtoInput;
import ru.practicum.shareit.request.dto.ItemRequestFullDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ItemRequestService requestService;

    private User user1;
    private ItemRequest itemRequest1;
    private ItemRequest itemRequest2;
    private ItemRequestFullDto itemRequestDto;


    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .id(1L)
                .name("User1 name")
                .email("user1@email.ru")
                .build();
        itemRequest1 = ItemRequest.builder()
                .id(1L)
                .description("Item request 1 description")
                .requestor(user1)
                .created(LocalDateTime.now())
                .build();
        itemRequestDto = ItemRequestMapper.toItemRequestWithItemsDto(itemRequest1, null);
        itemRequest2 = ItemRequest.builder()
                .id(2L)
                .description("Item request 2 description")
                .requestor(user1)
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    void addNew_whenValid_thenOk() throws Exception {
        Long userId = user1.getId();
        String desc = itemRequest1.getDescription();
        Mockito
                .when(requestService.create(anyLong(), Mockito.any()))
                .thenReturn(itemRequestDto);
        mockMvc.perform(post("/requests")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(
                                ItemRequestDtoInput.builder()
                                        .description(desc)
                                        .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequest1.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequest1.getDescription())))
                .andExpect(jsonPath("$.requestor.id", is(itemRequest1.getRequestor().getId()), Long.class))
                .andExpect(jsonPath("$.requestor.name", is(itemRequest1.getRequestor().getName())))
                .andExpect(jsonPath("$.requestor.email", is(itemRequest1.getRequestor().getEmail())))
                .andExpect(jsonPath("$.created", notNullValue()));
        Mockito.verify(requestService, Mockito.only())
                .create(anyLong(), Mockito.any());
        Mockito.verifyNoMoreInteractions(requestService);
    }

    @Test
    void getSort() throws Exception {
        Long userId = user1.getId();
        Integer defaultFrom = 0;
        Integer defaultSize = 20;
        List<ItemRequestFullDto> result = new ArrayList<>();
        result.add(ItemRequestMapper.toItemRequestWithItemsDto(itemRequest1, null));
        result.add(ItemRequestMapper.toItemRequestWithItemsDto(itemRequest2, null));

        Mockito
                .when(requestService.getSort(userId, defaultFrom, defaultSize))
                .thenReturn(result);
        mockMvc.perform(get("/requests/all")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", defaultFrom.toString())
                        .param("size", defaultSize.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].id", is(itemRequest1.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequest1.getDescription())))
                .andExpect(jsonPath("$[0].created", notNullValue()))
                .andExpect(jsonPath("$[0].items").hasJsonPath())
                .andExpect(jsonPath("$[1].id", is(itemRequest2.getId()), Long.class))
                .andExpect(jsonPath("$[1].description", is(itemRequest2.getDescription())))
                .andExpect(jsonPath("$[1].created", notNullValue()))
                .andExpect(jsonPath("$[0].items").hasJsonPath());
        Mockito.verify(requestService, Mockito.only())
                .getSort(userId, defaultFrom, defaultSize);
        Mockito.verifyNoMoreInteractions(requestService);
    }

    @Test
    void getSortWithWrongParams() throws Exception {
        Long userId = user1.getId();
        int from = -10;
        int size = 20;
        mockMvc.perform(get("/requests/all")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", Integer.toString(from))
                        .param("size", Integer.toString(size)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById() throws Exception {
        Long requestId = itemRequest1.getId();
        Long userId = user1.getId();
        Mockito
                .when(requestService.getById(requestId, userId))
                .thenReturn(itemRequestDto);
        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequest1.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequest1.getDescription())))
                .andExpect(jsonPath("$.created", notNullValue()))
                .andExpect(jsonPath("$.items").hasJsonPath());
        Mockito.verify(requestService, Mockito.only())
                .getById(requestId, userId);
        Mockito.verifyNoMoreInteractions(requestService);
    }
}
