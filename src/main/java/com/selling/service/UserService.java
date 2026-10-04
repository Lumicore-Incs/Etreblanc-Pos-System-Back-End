package com.selling.service;

import com.selling.dto.UserDto;
import com.selling.dto.get.UserDtoForGet;

import java.util.List;

public interface UserService {

  UserDto getUserById(String id);

  UserDto userLogin(UserDto dto);

  UserDto findUserByName(String email, String userName);

  UserDtoForGet registerUser(UserDto userDto);

  UserDtoForGet createUser(UserDto userDto);

  UserDtoForGet updateUser(UserDto userDto, Long userId);

  List<UserDtoForGet> getAllUser();

  UserDtoForGet changePassword(String email, String newPassword);

  boolean sendOtpToEmail(String email);

  boolean validateOtp(String email, String otp);

  boolean deleteUser(Integer id);

    List<UserDtoForGet> getAllUserWithOutAdmin();
}
