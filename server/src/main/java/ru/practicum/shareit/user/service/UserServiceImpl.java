package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        log.debug("Вызов метода create");
        if (userDto.getEmail() == null) {
            throw new ObjectNotFoundException("Отсутствует электронная почта");
        }
        User user = UserMapper.fromUserDto(userDto);
        user = userRepository.save(user);
        log.trace("Создан пользователь с id = {}", user.getId());
        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto get(Long id) {
        log.debug("Вызов метода get с id = {}", id);
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            log.trace("Завершение вызова метода get");
            return UserMapper.toUserDto(user.get());
        } else {
            throw new ObjectNotFoundException("Пользователь с id = " + id + " не найден");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAll() {
        log.debug("Вызов метода getAll");
        log.trace("Завершение вызова метода getAll");
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDto update(Long id, UserDto userDto) {
        log.debug("Вызов метода update с id = {}", id);
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new ObjectNotFoundException("Пользователь с id = " + id + " не найден");
        } else {
            User newUser = user.get();
            if (!checkUserEmail(userDto.getEmail(), id)) {
                throw new ConflictException("Электронная почта уже занята");
            }

            if (userDto.getName() != null && !Objects.equals(userDto.getName(), "")) {
                newUser.setName(userDto.getName());
            }
            if (userDto.getEmail() != null && !Objects.equals(userDto.getEmail(), "")) {
                newUser.setEmail(userDto.getEmail());
            }
            log.trace("Завершение вызова метода update");
            User result = userRepository.save(newUser);
            return UserMapper.toUserDto(result);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.debug("Вызов метода delete");
        userRepository.deleteById(id);
        log.trace("Завершение вызова метода delete");
    }

    public boolean checkUserEmail(String email, Long id) {
        log.trace("Вызов метода checkUserEmail с email = {}, id = {}", email, id);
        List<User> sameEmailUsers = userRepository.findByEmail(email);
        if (sameEmailUsers.isEmpty())
            return true;
        for (User user : sameEmailUsers) {
            if (!Objects.equals(user.getId(), id)) {
                return false;
            }
        }
        return true;
    }
}
