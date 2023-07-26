package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.booking.dto.BookingUserDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {
    private UserDto user1;
    private UserDto user2;
    private ItemDto item1;
    private ItemDto item2;
    private BookingUserDto booking1;
    private BookingUserDto booking2;

    private BookingDtoInput bookingInput;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    BookingService bookingService;

    @BeforeEach
    void setUp(WebApplicationContext wac) {
        user1 = UserDto.builder()
                .id(1L)
                .name("User 1 name")
                .email("user1@email.com")
                .build();
        user2 = UserDto.builder()
                .id(1L)
                .name("User 1 name")
                .email("user1@email.com")
                .build();
        item1 = ItemDto.builder()
                .id(1L)
                .name("Item1")
                .description("Item 1 description")
                .available(true)
                .build();
        item2 = ItemDto.builder()
                .id(1L)
                .name("Item1")
                .description("Item 1 description")
                .available(true)
                .build();
        bookingInput = BookingDtoInput.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .itemId(item1.getId())
                .build();
        booking1 = BookingUserDto.builder()
                .id(1L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .item(item1)
                .booker(user1)
                .status(BookingStatus.WAITING)
                .build();
        booking2 = BookingUserDto.builder()
                .id(2L)
                .start(LocalDateTime.now().plusDays(2).plusHours(1))
                .end(LocalDateTime.now().plusDays(2).plusHours(2))
                .item(item2)
                .booker(user2)
                .status(BookingStatus.APPROVED)
                .build();
    }

    @Test
    @DisplayName("Создание бронирования")
    void createBooking() throws Exception {
                when(bookingService.create(anyLong(), Mockito.any(BookingDtoInput.class)))
                .thenReturn(booking1);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(bookingInput))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(booking1.getId()), Long.class))
                .andExpect(jsonPath("$.start", Matchers.notNullValue()))
                .andExpect(jsonPath("$.end", Matchers.notNullValue()))
                .andExpect(jsonPath("$.status", is("WAITING")));
    }

    @Test
    @DisplayName("Подтверждение бронирования")
    void confirmBooking() throws Exception {
        when(bookingService.confirm(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(booking2);

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1)
                        .param("approved", "true")
                        .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(booking2.getId()), Long.class))
                .andExpect(jsonPath("$.start", Matchers.notNullValue()))
                .andExpect(jsonPath("$.end", Matchers.notNullValue()))
                .andExpect(jsonPath("$.status", is("APPROVED")));
    }

    @Test
    @DisplayName("Получение бронирования")
    void getBooking() throws Exception {
        when(bookingService.get(anyLong(), anyLong()))
                .thenReturn(booking1);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(booking1.getId()), Long.class))
                .andExpect(jsonPath("$.start", Matchers.notNullValue()))
                .andExpect(jsonPath("$.end", Matchers.notNullValue()))
                .andExpect(jsonPath("$.status", is("WAITING")));
    }

    @Test
    @DisplayName("Получение забронированных предметов")
    void getBookerBookings() throws Exception {
        List<BookingUserDto> bookings = new ArrayList<>();
        bookings.add(booking1);
        when(bookingService.getAllBookerBookings(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(bookings);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "ALL")
                        .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(booking1.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", Matchers.notNullValue()))
                .andExpect(jsonPath("$[0].end", Matchers.notNullValue()))
                .andExpect(jsonPath("$[0].status", is("WAITING")));
    }

    @DisplayName("Получение списка бронирования для предметов пользователя")
    @Test
    void getOwnerBookings() throws Exception {
        List<BookingUserDto> bookings = new ArrayList<>();
        bookings.add(booking1);
        when(bookingService.getAllOwnerBookings(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(bookings);

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "ALL")
                        .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(booking1.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", Matchers.notNullValue()))
                .andExpect(jsonPath("$[0].end", Matchers.notNullValue()))
                .andExpect(jsonPath("$[0].status", is("WAITING")));
    }
}