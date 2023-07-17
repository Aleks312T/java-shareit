package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

    @Transactional
    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        Optional<User> user = userRepository.findById(userId);
        if(user.isPresent()) {
            //TODO добавить проверки
            Item item = ItemMapper.fromItemDto(itemDto);
            item.setOwner(user.get());
            item = itemRepository.save(item);
            return ItemMapper.toItemDto(item);
        }
        else {
            throw new ObjectNotFoundException("Пользователь с id = " + userId + " не найден");
        }

    }

    @Transactional
    @Override
    public ItemDto get(Long id) {
        Optional<Item> item = itemRepository.findById(id);
        if(item.isPresent())
            return ItemMapper.toItemDto(item.get());
        else {
            throw new ObjectNotFoundException("Предмет с id = " + id + " не найден");
        }
    }

    @Transactional
    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        Optional<User> user = userRepository.findById(userId);
        if(user.isPresent()) {
            //TODO добавить проверки
            Item item = ItemMapper.fromItemDto(itemDto);
            if (itemDto.getName() != null) {
                item.setName(itemDto.getName());
            }
            if (itemDto.getDescription() != null) {
                item.setDescription(itemDto.getDescription());
            }
            if (itemDto.getAvailable() != null) {
                item.setAvailable(itemDto.getAvailable());
            }
            item = itemRepository.save(item);
            return ItemMapper.toItemDto(item);
        }
        else {
            throw new ObjectNotFoundException("Пользователь с id = " + userId + " не найден");
        }

    }

    @Transactional
    @Override
    public void delete(Long id) {
        itemRepository.deleteById(id);
    }

    @Transactional
    @Override
    public List<ItemDto> getAllUserItems(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if(user.isPresent()) {
            //TODO переписать при обновлении функционала Item
            List<Item> items = itemRepository.findAllByOwnerId(userId);
            List<ItemDto> result = new ArrayList<>();
            for(Item item : items) {
                result.add(ItemMapper.toItemDto(item));
            }
            return result;
        }
        else {
            throw new ObjectNotFoundException("Пользователь с id = " + userId + " не найден");
        }
    }

    @Transactional
    @Override
    public List<ItemDto> search(String text) {
        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}
