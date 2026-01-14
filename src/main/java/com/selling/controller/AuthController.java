package com.selling.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.selling.dto.ResetPasswordDto;
import com.selling.dto.UserDto;
import com.selling.dto.get.UserDtoForGet;
import com.selling.service.UserService;
import com.selling.util.JWTTokenGenerator;
import com.selling.util.TokenStatus;

@CrossOrigin()
@RestController
@RequestMapping("/user")
public class AuthController {

  private final UserService userService;

  private final JWTTokenGenerator jwtTokenGenerator;

  public AuthController(UserService userService, JWTTokenGenerator jwtTokenGenerator) {
    this.userService = userService;
    this.jwtTokenGenerator = jwtTokenGenerator;
  }

  @PostMapping("/login")
  public ResponseEntity<Object> postLogin(@RequestBody UserDto dto) {
    UserDto user = userService.userLogin(dto);
    Map<String, String> response = new HashMap<>();
    if (user == null) {
      response.put("massage", "wrong details");
      return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    } else {
      String token = this.jwtTokenGenerator.generateJwtToken(user);
      response.put("token", token);
      return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
  }

  @PostMapping("/register")
  public ResponseEntity<Object> registerUser(@RequestBody UserDto userDto) {
    UserDto isUser = this.userService.findUserByName(userDto.getEmail(), userDto.getName());
    if (isUser == null) {
      UserDtoForGet dto = this.userService.registerUser(userDto);
      return new ResponseEntity<>(dto, HttpStatus.CREATED);
    } else {
      System.out.println(isUser);
      return new ResponseEntity<>("Email is Allready exist", HttpStatus.BAD_REQUEST);
    }
  }

  @PostMapping("/create")
  public ResponseEntity<Object> createUser(@RequestBody UserDto userDto) {
    try {
      UserDtoForGet dto = this.userService.createUser(userDto);
      return new ResponseEntity<>(dto, HttpStatus.CREATED);
    } catch (IllegalArgumentException iae) {
      return new ResponseEntity<>(iae.getMessage(), HttpStatus.BAD_REQUEST);
    } catch (RuntimeException re) {
      // treat as conflict if it's email exists
      if (re.getMessage() != null && re.getMessage().toLowerCase().contains("email")) {
        return new ResponseEntity<>(re.getMessage(), HttpStatus.CONFLICT);
      }
      return new ResponseEntity<>(re.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (Exception e) {
      return new ResponseEntity<>("Internal server error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping("/get_user_info_by_token")
  public ResponseEntity<Object> getUserInfoByToken(@RequestHeader(name = "Authorization") String authorizationHeader) {
    if (this.jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
      UserDto userFromJwtToken = this.jwtTokenGenerator.getUserFromJwtToken(authorizationHeader);
      return new ResponseEntity<>(userFromJwtToken, HttpStatus.CREATED);
    } else {
      return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
    }
  }

  @PutMapping("/update/{userId}")
  public ResponseEntity<Object> updateUser(@PathVariable Long userId, @RequestBody UserDto userDto,
      @RequestHeader(name = "Authorization") String authorizationHeader) {
    if (this.jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
      UserDtoForGet dto = this.userService.updateUser(userDto, userId);
      return new ResponseEntity<>(dto, HttpStatus.CREATED);
    } else {
      return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
    }
  }

  @GetMapping("/get_all_user")
  public ResponseEntity<Object> getAllUser(@RequestHeader(name = "Authorization") String authorizationHeader) {
    if (this.jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
      UserDto userFromJwtToken = jwtTokenGenerator.getUserFromJwtToken(authorizationHeader);
      List<UserDtoForGet> allUsers=null;
      if (Objects.equals(userFromJwtToken.getRole(), "ADMIN")) {
        allUsers = this.userService.getAllUser();
      }else {
        allUsers = this.userService.getAllUserWithOutAdmin();
      }

      return new ResponseEntity<>(allUsers, HttpStatus.CREATED);
    } else {
      return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
    }
  }

  @PostMapping("/send")
  public String sendOtp(@RequestParam String email) {
    boolean isSave = userService.sendOtpToEmail(email);
    if (isSave) {
      return "OTP sent successfully to " + email;
    } else {
      return "OTP sent failed email check again..!";
    }
  }

  @PostMapping("/validate")
  public String validateOtp(@RequestParam String email, @RequestParam String otp) {
    boolean isValid = userService.validateOtp(email, otp);
    if (isValid) {
      return "OTP is valid";
    } else {
      return "OTP is invalid";
    }
  }

  @PostMapping("/reset")
  public String resetPassword(@RequestBody ResetPasswordDto dto) {

    // First validate OTP
    boolean isValid = userService.validateOtp(dto.getEmail(), dto.getOtp());
    if (!isValid) {
      throw new RuntimeException("Invalid OTP");
    }

    // Change password
    userService.changePassword(dto.getEmail(), dto.getPassword());

    return "Password changed successfully";
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Object> deleteUser(@RequestHeader(name = "Authorization") String authorizationHeader,
      @PathVariable Integer id) {
    try {
      if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
        return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
      }
      UserDto userDto = jwtTokenGenerator.getUserFromJwtToken(authorizationHeader);
      if (Objects.equals(userDto.getRole(), "admin") || Objects.equals(userDto.getRole(), "ADMIN")
          || Objects.equals(userDto.getRole(), "Admin")) {
        boolean isDeleted = userService.deleteUser(id);

        if (isDeleted) {
          return new ResponseEntity<>("User disabled successfully", HttpStatus.OK);
        } else {
          return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
      }
      return new ResponseEntity<>("Customer not found", HttpStatus.NOT_FOUND);
    } catch (Exception e) {
      return new ResponseEntity<>("Error retrieving products: " + e.getMessage(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
