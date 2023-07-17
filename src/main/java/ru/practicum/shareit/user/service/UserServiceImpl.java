package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Transactional
    @Override
    public UserDto create(UserDto userDto) {
        User user = userRepository.save(UserMapper.fromUserDto(userDto));
        return UserMapper.toUserDto(user);
    }

    @Transactional
    @Override
    public UserDto get(Long id) {
        Optional<User> user = userRepository.findById(id);
        if(user.isPresent())
            return UserMapper.toUserDto(user.get());
        else {
            throw new ObjectNotFoundException("Пользователь с id = " + id + " не найден");
        }
    }

    @Transactional
    @Override
    public List<UserDto> getAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public UserDto update(Long id,UserDto userDto) {
        Optional<User> user = userRepository.findById(id);
        if(user.isEmpty())
            throw new ObjectNotFoundException("Пользователь с id = " + id + " не найден");
        else {
            User newUser = user.get();
            if (userDto.getName() != null) {
                newUser.setName(userDto.getName());
            }
            if (userDto.getEmail() != null) {
                newUser.setEmail(userDto.getEmail());
            }
            return UserMapper.toUserDto(userRepository.save(newUser));
        }

    }

    @Transactional
    @Override
    public void delete(Long id) {
        //TODO вернуть потом эту проверку
//        List<Item> empty = new ArrayList<>();
//        if (itemRepository.getAllItemUsers(id).equals(empty))
//            userRepository.delete(id);
//        else
//            throw new RuntimeException("Нельзя удалить пользователя, у которого есть вещи");
        userRepository.deleteById(id);
    }
}
