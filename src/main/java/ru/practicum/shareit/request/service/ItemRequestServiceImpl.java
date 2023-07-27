package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.IncorrectParameterException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDtoInput;
import ru.practicum.shareit.request.dto.ItemRequestFullDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// TODO добавить логирование
@Service
@RequiredArgsConstructor
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestFullDto create(Long userId, ItemRequestDtoInput requestDtoInput) {
        //Не использую функцию, чтобы не обращаться к БД два раза
        //checkUserId(userId);
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new ObjectNotFoundException("Пользователь с Id = " + userId + " не найден");
        }
        ItemRequest itemRequest = ItemRequestMapper.fromItemRequestDtoInput(
                requestDtoInput,
                user.get(),
                LocalDateTime.now());

        itemRequest = itemRequestRepository.save(itemRequest);
        log.trace("");
        return ItemRequestMapper.toItemRequestWithItemsDto(itemRequest, new ArrayList<>());
    }

    @Override
    @Transactional
    public List<ItemRequestFullDto> getAll(Long userId) {
        checkUserId(userId);
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequestor_Id(userId);
        return toItemRequestFullDtoResponse(itemRequests);
    }

    @Override
    @Transactional
    public List<ItemRequestFullDto> getSort(Long userId, Integer from, Integer size) {
        if (from % size != 0) {
            throw new IncorrectParameterException("Некорректный ввод страниц и размеров");
        }
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size, Sort.by("created").descending());
        Page<ItemRequest> itemRequestPage = itemRequestRepository.findAllByRequestor_IdNot(userId, pageable);
        List<ItemRequest> itemRequests = itemRequestPage.getContent();

        return toItemRequestFullDtoResponse(itemRequests);
    }

    @Override
    @Transactional
    public ItemRequestFullDto getById(Long userId, Long requestId) {
        checkUserId(userId);

        ItemRequest itemRequest = itemRequestRepository.findById(requestId).orElseThrow(
                () -> new ObjectNotFoundException("Запрос на предмет с Id = " + requestId + " не найден."));
        List<ItemDto> itemsForRequestDto = itemRepository.findAllByRequestId(requestId)
                        .stream()
                        .map(ItemMapper::toItemDto)
                        .collect(Collectors.toList());
        return ItemRequestMapper.toItemRequestWithItemsDto(itemRequest, itemsForRequestDto);
    }

    public void checkUserId(Long id) {
        log.trace("Вызов метода checkUserEmail с Long = {}", id);
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new ObjectNotFoundException("Пользователь с Id = " + id + " не найден");
        }
    }

    private List<ItemRequestFullDto> toItemRequestFullDtoResponse(List<ItemRequest> itemRequests) {
        return itemRequests.isEmpty() ? Collections.emptyList() : itemRequests.stream()
                .map(itemRequest -> {
                    List<Item> items = itemRepository.findAllByRequestId(itemRequest.getId());
                    List<ItemDto> itemsDto = items.stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
                    return ItemRequestMapper.toItemRequestWithItemsDto(itemRequest, itemsDto);
                })
                .collect(Collectors.toList());
    }
}
