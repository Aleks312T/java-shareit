package ru.practicum.shareit.request.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

@UtilityClass
public class ItemRequestMapper {
    public static ItemRequestDtoInput toItemRequestDtoInput(ItemRequest request) {
        return ItemRequestDtoInput.builder()
                .id(request.getId())
                .description(request.getDescription())
                .userId(request.getRequestor().getId())
                .build();
    }

    public static ItemRequestFullDto toItemRequestWithItemsDto(ItemRequest itemRequest,
                                                               List<ItemDto> items) {
        return ItemRequestFullDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .requestor(itemRequest.getRequestor())
                .created(itemRequest.getCreated())
                .items(items)
                .build();
    }

}
