package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comments.dto.CommentDto;
import ru.practicum.shareit.comments.dto.CommentMapper;
import ru.practicum.shareit.comments.model.Comment;
import ru.practicum.shareit.comments.repository.CommentRepository;
import ru.practicum.shareit.exceptions.IncorrectParameterException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;

    private final UserRepository userRepository;

    private final CommentRepository commentRepository;

    private final BookingRepository bookingRepository;

    // TODO добавить логирование
    @Transactional
    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        log.debug("Вызов метода create");
        User user = checkUser(userId);
        Item item = ItemMapper.fromItemDto(itemDto);
        item.setOwner(user);
        item = itemRepository.save(item);
        log.trace("Создан предмет с id = {}", item.getId());
        return ItemMapper.toItemDto(item);
    }

    @Transactional
    @Override
    public ItemDto get(Long itemId, Long userId) {
        log.debug("Вызов метода get с itemId = {}, userId = {}", itemId, userId);
        Item item = checkItem(itemId);
        User user = checkUser(userId);

        ItemDto result = addBookingAndComment(item, userId);
        log.trace("Завершение вызова метода get");
        return result;
    }

    @Transactional
    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        log.debug("Вызов метода update с itemId = {}, userId = {}", itemId, userId);
        User user = checkUser(userId);
        Item result = checkItem(itemId);
        if (itemDto.getName() != null) {
            result.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            result.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            result.setAvailable(itemDto.getAvailable());
        }
        result = itemRepository.save(result);
        log.trace("Завершение вызова метода update");
        return ItemMapper.toItemDto(result);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        log.debug("Вызов метода delete с id = {}", id);
        itemRepository.deleteById(id);
        log.trace("Завершение вызова метода delete");
    }

    @Transactional
    @Override
    public List<ItemDto> getAllUserItems(Long userId) {
        log.debug("Вызов метода getAllUserItems с userId = {}", userId);
        User user = checkUser(userId);
        List<Item> items = itemRepository.findAllByOwnerId(userId);
        List<ItemDto> result = new ArrayList<>();
        for (Item item : items) {
            result.add(addBookingAndComment(item, userId));
        }
        log.trace("Завершение вызова метода getAllUserItems");
        return result;
    }

    @Transactional
    @Override
    public List<ItemDto> search(String text) {
        log.debug("Вызов метода search");
        if (text == null)
            throw new NullPointerException("Отсутствует входной текст");
        else
        if (text.isBlank() || text.isEmpty())
            return new ArrayList<>();
        else {
            List<ItemDto> result =  itemRepository.search(text).stream()
                    .map(ItemMapper::toItemDto)
                    .collect(Collectors.toList());
            log.trace("Завершение вызова метода search");
            return result;
        }
    }

    @Transactional
    @Override
    public CommentDto createComment(CommentDto commentDto, Long userId, Long itemId) {
        log.debug("Вызов метода createComment с itemId = {}, userId = {}", itemId, userId);
        User user = checkUser(userId);
        Item item = checkItem(itemId);
        if (!bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndBefore(
                itemId, userId, BookingStatus.APPROVED, LocalDateTime.now()))
            throw new ValidationException("Невозможно оставить комментарий");

        if (commentDto.getText() == null || commentDto.getText().isBlank())
            throw new IncorrectParameterException("Отсутствует входной текст");
        Comment comment = CommentMapper.fromCommentDto(commentDto, item, user);
        comment = commentRepository.save(comment);
        log.trace("Завершение вызова метода createComment");
        return CommentMapper.toCommentDto(comment);
    }

    private ItemDto addBookingAndComment(Item item, Long userId) {
        log.trace("Вызов метода addBookingAndComment с itemId = {}, userId = {}", item.getId(), userId);
        Booking lastBooking = null;
        Booking nextBooking = null;

        if (userId.equals(item.getOwner().getId())) {
            lastBooking = bookingRepository.getLastBooking(item.getId());
            if (lastBooking == null || lastBooking.getBooker().getId().equals(item.getOwner().getId())) {
                lastBooking = null;
            } else {
                nextBooking = bookingRepository.getNextBooking(item.getId(), lastBooking.getEnd());
            }
        }
        List<CommentDto> comments = CommentMapper.fromListComment(commentRepository.findAllByItemId(item.getId()));

        BookingItemDto last = (lastBooking == null ? null : BookingMapper.toBookingItemDto(lastBooking));
        BookingItemDto next = (nextBooking == null ? null : BookingMapper.toBookingItemDto(nextBooking));

        return ItemMapper.toItemDtoAll(item, last, next, comments);
    }

    public User checkUser(Long userId) {
        log.trace("Вызов метода checkUser с userId = {}", userId);
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            return user.get();
        } else {
            throw new ObjectNotFoundException("Пользователь с id = " + userId + " не найден");
        }
    }

    public Item checkItem(Long itemId) {
        log.trace("Вызов метода checkItem с itemId = {}", itemId);
        Optional<Item> item = itemRepository.findById(itemId);
        if (item.isPresent()) {
            return item.get();
        } else {
            throw new ObjectNotFoundException("Предмет с id = " + itemId + " не найден");
        }
    }
}
