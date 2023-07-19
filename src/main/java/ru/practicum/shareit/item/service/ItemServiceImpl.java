package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comments.dto.CommentDto;
import ru.practicum.shareit.comments.dto.CommentMapper;
import ru.practicum.shareit.comments.model.Comment;
import ru.practicum.shareit.comments.repository.CommentRepository;
import ru.practicum.shareit.exceptions.IncorrectParameterException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.transaction.Transactional;
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
        // TODO добавить проверки входных данных
        User user = checkUser(userId);
        Item item = ItemMapper.fromItemDto(itemDto);
        item.setOwner(user);
        item = itemRepository.save(item);
        return ItemMapper.toItemDto(item);
    }

    @Transactional
    @Override
    public ItemDto get(Long itemId, Long userId) {
        Item item = checkItem(itemId);
        User user = checkUser(userId);

        ItemDto result = addBookingAndComment(item, userId);
        return result;
    }

    @Transactional
    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        User user = checkUser(userId);
        // TODO добавить проверки
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
        return ItemMapper.toItemDto(result);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        itemRepository.deleteById(id);
    }

    @Transactional
    @Override
    public List<ItemDto> getAllUserItems(Long userId) {
        User user = checkUser(userId);
        // TODO переписать при обновлении функционала Item
        List<Item> items = itemRepository.findAllByOwnerId(userId);
        List<ItemDto> result = new ArrayList<>();
        for(Item item : items) {
            result.add(addBookingAndComment(item, userId));
        }
        return result;
    }

    @Transactional
    @Override
    public List<ItemDto> search(String text) {
        if(text == null)
            throw new NullPointerException("Отсутствует входной текст");
        else
        if(text.isBlank() || text.isEmpty())
            return new ArrayList<>();
        else
            return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public CommentDto createComment(CommentDto commentDto, Long userId, Long itemId) {
        User user = checkUser(userId);
        Item item = checkItem(itemId);
        if(commentDto.getText() == null || commentDto.getText().isBlank())
            throw new IncorrectParameterException("Отсутствует входной текст");
        if(commentDto.getAuthorName() == null || commentDto.getAuthorName().isBlank())
            throw new IncorrectParameterException("Отсутствует автор");
        Comment comment = CommentMapper.fromCommentDto(commentDto, item, user);
        comment = commentRepository.save(comment);
        return CommentMapper.toCommentDto(comment);
    }

    private ItemDto addBookingAndComment(Item item, Long userId) {
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
        BookingItemDto next = (lastBooking == null ? null : BookingMapper.toBookingItemDto(nextBooking));

        return ItemMapper.toItemDtoAll(item, last, next, comments);
    }

    public User checkUser(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if(user.isPresent()) {
            return user.get();
        }
        else {
            throw new ObjectNotFoundException("Пользователь с id = " + userId + " не найден");
        }
    }

    public Item checkItem(Long itemId) {
        Optional<Item> item = itemRepository.findById(itemId);
        if(item.isPresent()) {
            return item.get();
        }
        else {
            throw new ObjectNotFoundException("Предмет с id = " + itemId + " не найден");
        }
    }
}
