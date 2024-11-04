package com.senla.readingbooks.service.interfaces.user;

import com.senla.readingbooks.dto.UserFilterDto;
import com.senla.readingbooks.dto.UsersAsPageDto;
import com.senla.readingbooks.dto.auth.ChangePasswordDto;
import com.senla.readingbooks.dto.user.UserReadDto;
import com.senla.readingbooks.dto.user.UserUpdateDto;
import com.senla.readingbooks.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    Page<UsersAsPageDto> findUsersAsPage(Pageable pageable, UserFilterDto filter);

    UserReadDto findUserById(Long id);

    UserReadDto updateUser(Long id, UserUpdateDto userUpdateDto);

    void deleteUser(Long id);

    List<User> findUsersByIds(List<Long> ids);

    void changePassword(Long id, ChangePasswordDto passwordDto);

    User findById(Long id);


}
