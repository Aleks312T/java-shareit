package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.ArrayList;

@Slf4j
@RestController
@RequestMapping("/items")
@Validated
public class ItemController {

    private final ItemClient itemClient;

    @Autowired
    public ItemController(ItemClient itemClient) {
        this.itemClient = itemClient;
    }

    @PostMapping
    public ResponseEntity<Object> newItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                          @RequestBody @Validated ItemDto itemDto) {
        log.trace("добавление предмета");
        return itemClient.newItem(userId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                          @PathVariable Long itemId) {
        log.trace("получение предмета c id №{}", itemId);
        return itemClient.getItem(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllItemUsers(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @PositiveOrZero
                                                  @RequestParam (value = "from", defaultValue = "0")
                                                  Long from,
                                                  @Positive
                                                  @RequestParam (value = "size", defaultValue = "10")
                                                  Long size) {
        log.trace("вывод всех предметов пользователя c id №{}", userId);
        return itemClient.getAllItemUsers(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @RequestParam(value = "text", required = false) String text,
                                             @PositiveOrZero
                                             @RequestParam(value = "from", defaultValue = "0")
                                             Long from,
                                             @Positive
                                             @RequestParam(value = "size", defaultValue = "10")
                                             Long size) {
        log.trace("поиск предмета по имени: {}",text);
        if (!text.isBlank()) {
            return itemClient.searchItem(userId, text, from, size);
        } else {
            return ResponseEntity.ok().body(new ArrayList<>());
        }
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long itemId,
                                             @RequestBody ItemDto itemDto) {
        log.trace("обновление предмета c id №{}",itemId);
        return itemClient.updateItem(userId, itemId, itemDto);
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@PathVariable Long itemId) {
        log.trace("удаление предмета с id №{}",itemId);
        itemClient.deleteItem(itemId);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> postComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @PathVariable Long itemId,
                                              @RequestBody @Valid CommentDto commentDto) {
        log.trace("Добавление комментария к предмету с id №{}",itemId);
        return itemClient.postComment(userId, itemId, commentDto);
    }

}