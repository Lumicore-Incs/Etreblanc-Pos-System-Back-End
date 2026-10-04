package com.selling.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.selling.dto.UserDto;
import com.selling.dto.get.UserDtoForGet;
import com.selling.model.Otp;
import com.selling.model.Product;
import com.selling.model.User;
import com.selling.repository.OtpRepo;
import com.selling.repository.ProductRepo;
import com.selling.repository.UserRepo;
import com.selling.service.UserService;
import com.selling.util.MailService;
import com.selling.util.MapperService;

@Service
public class UserServiceImpl implements UserService {

  private final UserRepo userRepo;
  private final MapperService mapperService;
  private final OtpRepo otpRepo;
  private final ProductRepo productRepo;
  private final MailService mailService;
  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  public UserServiceImpl(UserRepo userRepo, OtpRepo otpRepo, ProductRepo productRepo, MailService mailService,
      MapperService mapperService) {
    this.userRepo = userRepo;
    this.otpRepo = otpRepo;
    this.productRepo = productRepo;
    this.mailService = mailService;
    this.mapperService = mapperService;
  }

  @Override
  public UserDto getUserById(String id) {
    User byId = userRepo.findUserById(Long.valueOf(id));
    return mapperService.map(byId, UserDto.class);
  }

  @Override
  public UserDto userLogin(UserDto dto) {
    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    List<User> userNames = userRepo.findAllByName(dto.getEmail());
    for (User name : userNames) {
      boolean isPasswordMatches = passwordEncoder.matches(dto.getPassword(), name.getPassword());
      if (isPasswordMatches) {
        return mapperService.map(name, UserDto.class);
      }
    }
    return null;
  }

  @Override
  public UserDto findUserByName(String email, String userName) {
    Optional<User> userDetails = userRepo.findByEmail(email);
    return userDetails.map(user -> mapperService.map(user, UserDto.class)).orElse(null);
  }

  @Override
  public UserDtoForGet registerUser(UserDto userDto) {
    User user = mapperService.map(userDto, User.class);
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    user.setStatus("ACTIVE");
    user.setRegistration_date(String.valueOf(LocalDateTime.now()));
    Optional<Product> byId = productRepo.findById(Long.valueOf(userDto.getProductId()));
    byId.ifPresent(user::setProduct);
    User save = this.userRepo.save(user);
    return mapperService.map(save, UserDtoForGet.class);
  }

  @Override
  public UserDtoForGet createUser(UserDto userDto) {
    // Basic validation
    if (userDto.getEmail() == null || userDto.getEmail().isBlank() || userDto.getPassword() == null
        || userDto.getPassword().isBlank() || userDto.getName() == null || userDto.getName().isBlank()) {
      throw new IllegalArgumentException("Missing required fields: name, email, or password");
    }

    // Check for existing email
    Optional<User> existing = userRepo.findByEmail(userDto.getEmail());
    if (existing.isPresent()) {
      throw new RuntimeException("Email already exists");
    }

    User user = mapperService.map(userDto, User.class);
    user.setId(null);
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    // Map product if provided
    if (userDto.getProductId() != null && !userDto.getProductId().isBlank()) {
      productRepo.findById(Long.valueOf(userDto.getProductId())).ifPresent(user::setProduct);
    }

    User save = userRepo.save(user);
    return mapperService.map(save, UserDtoForGet.class);
  }

  @Override
  public UserDtoForGet updateUser(UserDto userDto, Long userId) {
    User byId = userRepo.findUserById(userId);
    if (byId == null) {
      return null;
    } else {
      userDto.setId(byId.getId());
      userDto.setRegistration_date(byId.getRegistration_date());
      userDto.setProductId(userDto.getProductId());
      userDto.setStatus(userDto.getStatus());
      if (userDto.getPassword()==null){
        System.out.println("1");
        userDto.setPassword(byId.getPassword());
      }else {
        System.out.println("2");
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
      }
      User user = mapperService.map(userDto, User.class);
      User save = userRepo.save(user);
      return mapperService.map(save, UserDtoForGet.class);
    }
  }

  @Override
  public List<UserDtoForGet> getAllUser() {
    List<User> all = userRepo.findAll();
    List<UserDtoForGet> allUsers = new ArrayList<>();
    for (User user : all) {
      UserDtoForGet userDto = mapperService.map(user, UserDtoForGet.class);
      allUsers.add(userDto);
    }
    return allUsers;
  }

  // otp part ===================================

  // Generate random 4-digit OTP
  private String generateOtp() {
    Random random = new Random();
    int otp = 1000 + random.nextInt(9000);
    return String.valueOf(otp);
  }

  // Send OTP to email
  public boolean sendOtpToEmail(String email) {
    try {
      Optional<User> user = userRepo.findByEmail(email);
      if (user.isPresent()) {
        Optional<Otp> byEmail = otpRepo.findByEmail(email);
        byEmail.ifPresent(otp -> otpRepo.deleteById(otp.getId()));

        // Generate new OTP
        String otp = generateOtp();
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(5);

        // Save OTP to database
        Otp otpEntity = new Otp(email, otp, expiryTime);
        otpRepo.save(otpEntity);

        // Send OTP via email
        return mailService.sendOtpEmail(email, otp);
      }
      return false;
    } catch (Exception e) {
      System.out.println("============");
      System.out.println(e.getMessage());
      return false;
    }
  }

  // Validate OTP
  public boolean validateOtp(String email, String otp) {
    Otp otpEntity = otpRepo.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("OTP not found for this email"));

    if (otpEntity.getExpiryTime().isBefore(LocalDateTime.now())) {
      throw new RuntimeException("OTP has expired");
    }

    return otpEntity.getOtpCode().equals(otp);
  }


  @Override
  public boolean deleteUser(Integer id) {
    User user = userRepo.findById(Long.valueOf(id))
        .orElseThrow(() -> new RuntimeException("User not found"));
    // user.setStatus("DISABLED");
    userRepo.delete(user);
    return true;
  }

  @Override
  public List<UserDtoForGet> getAllUserWithOutAdmin() {
    List<User> all = userRepo.findAll();
    List<UserDtoForGet> allUsers = new ArrayList<>();
    for (User user : all) {
      if (!user.getRole().equals("ADMIN")){
        UserDtoForGet userDto = mapperService.map(user, UserDtoForGet.class);
        allUsers.add(userDto);
      }
    }
    return allUsers;
  }

  @Override
  public UserDtoForGet changePassword(String email, String newPassword) {
    User user = userRepo.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found"));

    user.setPassword(passwordEncoder.encode(newPassword));
    User save = userRepo.save(user);
    return mapperService.map(save, UserDtoForGet.class);
  }

  // mapping done inline via MapperService to reduce boilerplate
}
