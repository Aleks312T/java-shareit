package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;

    private final UserRepository userRepository;

    @Override
    public ItemDto create(Integer userId, ItemDto itemDto) {
        User user = userRepository.get(userId);
        Item item = itemRepository.create(user,ItemMapper.fromItemDto(itemDto));
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto get(Integer id) {
        Item item = itemRepository.get(id);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto update(Integer userId, Integer itemId, ItemDto itemDto) {
        User user = userRepository.get(userId);
        Item item =  itemRepository.update(user.getId(),itemId,ItemMapper.fromItemDto(itemDto));
        return ItemMapper.toItemDto(item);
    }

    @Override
    public void delete(Integer id) {
        itemRepository.delete(id);
    }

    @Override
    public List<ItemDto> getAllItemUsers(Integer userId) {
        User user = userRepository.get(userId);
        List<Item> items = itemRepository.getAllItemUsers(user.getId());
        List<ItemDto> itemsDto = new ArrayList<>();
        for (Item i : items) {
            itemsDto.add(ItemMapper.toItemDto(i));
        }
        return itemsDto;
    }

    @Override
    public List<ItemDto> search(String text) {
        List<Item> items = itemRepository.search(text);
        List<ItemDto> itemsDto = new ArrayList<>();
        for (Item i : items) {
            itemsDto.add(ItemMapper.toItemDto(i));
        }
        return itemsDto;
    }
}
