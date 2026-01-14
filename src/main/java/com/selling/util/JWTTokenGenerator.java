package com.selling.util;

import java.security.Key;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.selling.dto.UserDto;
import com.selling.service.UserService;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JWTTokenGenerator {
  private static final Logger logger = LoggerFactory.getLogger(JWTTokenGenerator.class);

  @Value("${jwt.secret}")
  private String jwtSecret;

  @Value("${jwt.refreshToken.expiration}")
  private int jwtExpirationMs;

  private final UserService userService;

  public JWTTokenGenerator(UserService userService) {
    this.userService = userService;
  }

  public String generateJwtToken(UserDto user) {
    return Jwts.builder()
        .setId(String.valueOf(user.getId()))
        .setSubject((user.getName()))
        .setIssuedAt(new Date())
        .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
        .signWith(key(), SignatureAlgorithm.HS256)
        .compact();
  }

  private Key key() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
  }

  public boolean validateJwtToken(String authToken) {
    String jwtToken = authToken.substring("Bearer ".length());
    try {
      Jwts.parserBuilder().setSigningKey(key()).build().parse(jwtToken);
      return true;
    } catch (Exception e) {
      logger.error("Invalid JWT token: {}", e.getMessage());
    }
    return false;
  }

  public UserDto getUserFromJwtToken(String token) {
    String jwtToken = token.substring("Bearer ".length());
    String id = Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(jwtToken).getBody().getId();
    return userService.getUserById(id);
  }
}
