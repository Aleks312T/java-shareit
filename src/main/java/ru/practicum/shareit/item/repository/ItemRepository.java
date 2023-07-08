package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface ItemRepository {

    Item create(User user, Item item);

    Item get(Integer id);

    Item update(Integer userId, Integer itemId, Item item);
}