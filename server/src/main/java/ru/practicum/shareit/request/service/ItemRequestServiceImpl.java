package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.IncorrectParameterException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        log.debug("Вызов метода create с userId = {}", userId);
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
        log.trace("Завершение вызова метода create");
        return ItemRequestMapper.toItemRequestWithItemsDto(itemRequest, new ArrayList<>());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestFullDto> getAll(Long userId) {
        log.debug("Вызов метода getAll с userId = {}", userId);
        checkUserId(userId);
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequestor_Id(userId);

        List<ItemRequestFullDto> result = toItemRequestFullDtoResponse(itemRequests);
        log.trace("Завершение вызова метода getAll");
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestFullDto> getSort(Long userId, Integer from, Integer size) {
        log.debug("Вызов метода getSort с userId = {}", userId);
        if (from % size != 0) {
            throw new IncorrectParameterException("Некорректный ввод страниц и размеров");
        }
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size, Sort.by("created").descending());
        Page<ItemRequest> itemRequestPage = itemRequestRepository.findAllByRequestor_IdNot(userId, pageable);
        List<ItemRequest> itemRequests = itemRequestPage.getContent();

        List<ItemRequestFullDto> result = toItemRequestFullDtoResponse(itemRequests);
        log.trace("Завершение вызова метода getSort");
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public ItemRequestFullDto getById(Long userId, Long requestId) {
        log.debug("Вызов метода getById с userId = {}, requestId = {}", userId, requestId);
        checkUserId(userId);

        ItemRequest itemRequest = itemRequestRepository.findById(requestId).orElseThrow(
                () -> new ObjectNotFoundException("Запрос на предмет с Id = " + requestId + " не найден."));
        List<ItemDto> itemsForRequestDto = itemRepository.findAllByRequestId(requestId)
                        .stream()
                        .map(ItemMapper::toItemDto)
                        .collect(Collectors.toList());

        ItemRequestFullDto result = ItemRequestMapper.toItemRequestWithItemsDto(itemRequest, itemsForRequestDto);
        log.trace("Завершение вызова метода getById");
        return result;
    }

    public void checkUserId(Long id) {
        log.trace("Вызов метода checkUserId с id = {}", id);
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new ObjectNotFoundException("Пользователь с Id = " + id + " не найден");
        }
    }

    private List<ItemRequestFullDto> toItemRequestFullDtoResponse(List<ItemRequest> itemRequests) {
        log.trace("Вызов метода toItemRequestFullDtoResponse");
        return itemRequests.isEmpty() ? Collections.emptyList() : itemRequests.stream()
                .map(itemRequest -> {
                    List<Item> items = itemRepository.findAllByRequestId(itemRequest.getId());
                    List<ItemDto> itemsDto = items.stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
                    return ItemRequestMapper.toItemRequestWithItemsDto(itemRequest, itemsDto);
                })
                .collect(Collectors.toList());
    }
}
