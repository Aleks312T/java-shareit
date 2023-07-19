package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.IncorrectParameterException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.transaction.Transactional;
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
        if(userDto.getEmail() == null) {
            throw new ObjectNotFoundException("Отсутствует электронная почта");
        }
        User user = UserMapper.fromUserDto(userDto);
        user = userRepository.save(user);
        log.trace("Создан пользователь с id = {}", user.getId());
        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public UserDto get(Long id) {
        log.debug("Вызов метода get с id = {}", id);
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            log.trace("Завершение вызова метода get");
            return UserMapper.toUserDto(user.get());
        }
        else {
            throw new ObjectNotFoundException("Пользователь с id = " + id + " не найден");
        }
    }

    @Override
    @Transactional
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
        if(user.isEmpty()) {
            throw new ObjectNotFoundException("Пользователь с id = " + id + " не найден");
        }
        else {
            User newUser = user.get();
            if(checkUserEmail(newUser.getEmail(), userDto.getId()))
                throw new IncorrectParameterException("Электронная почта уже занята");
            if (userDto.getName() != null) {
                newUser.setName(userDto.getName());
            }
            if (userDto.getEmail() != null) {
                newUser.setEmail(userDto.getEmail());
            }
            log.trace("Завершение вызова метода update");
            return UserMapper.toUserDto(userRepository.save(newUser));
        }

    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.debug("Вызов метода delete");
        userRepository.deleteById(id);
        log.trace("Завершение вызова метода delete");
    }

    public void checkUserEmail(String email) {
        log.trace("Вызов метода checkUserEmail с email = {}", email);
        List<User> sameEmailUsers = userRepository.findByEmailContainingIgnoreCase(email);
        if(!sameEmailUsers.isEmpty()) {
            throw new IncorrectParameterException("Электронная почта уже занята");
        }
    }

    public boolean checkUserEmail(String email, Long id) {
        log.trace("Вызов метода checkUserEmail с email = {}, id = {}", email, id);
        List<User> sameEmailUsers = userRepository.findByEmailContainingIgnoreCase(email);
        for (User user : sameEmailUsers) {
            if (!Objects.equals(user.getId(), id)) {
                return false;
            }
        }
        return true;
    }
}
