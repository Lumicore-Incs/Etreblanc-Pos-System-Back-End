package com.selling.controller;

import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.selling.dto.LeadRequestDto;
import com.selling.dto.LeadStatusRequestDto;
import com.selling.dto.UserDto;
import com.selling.dto.get.LeadDtoGet;
import com.selling.dto.get.LeadQtyDtoGet;
import com.selling.service.LeadService;
import com.selling.util.JWTTokenGenerator;
import com.selling.util.TokenStatus;

import lombok.RequiredArgsConstructor;

@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/lead")
public class LeadController {
    private final JWTTokenGenerator jwtTokenGenerator;
    private final LeadService leadService;

    @PostMapping
    public ResponseEntity<Object> saveLead(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @RequestBody List<LeadRequestDto> requests) {
        if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
            return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
        }
        UserDto userDto = jwtTokenGenerator.getUserFromJwtToken(authorizationHeader);
        boolean administrator = isAdministrator(userDto);
        if (!administrator && requests.stream()
                .filter(Objects::nonNull)
                .anyMatch(request -> request.getUserId() != null
                        && !Objects.equals(request.getUserId(), userDto.getId()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot create a lead for another user");
        }

        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(leadService.saveLead(requests, userDto.getId(), administrator));
        } catch (ResponseStatusException exception) {
            return statusError(exception);
        }
    }

    @GetMapping("/getQty")
    public ResponseEntity<Object> getLeadQty(
            @RequestHeader(name = "Authorization") String authorizationHeader) {
        if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
            return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
        }
        UserDto user = jwtTokenGenerator.getUserFromJwtToken(authorizationHeader);
        if (!isAdministrator(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Administrator access required");
        }
        List<LeadQtyDtoGet> quantity = leadService.getLeadQty();
        return ResponseEntity.ok(quantity);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Object> getLeadsByUser(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @PathVariable Long userId) {
        if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
            return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
        }
        UserDto user = jwtTokenGenerator.getUserFromJwtToken(authorizationHeader);
        if (!isAdministrator(user) && !Objects.equals(user.getId(), userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot access another user's leads");
        }
        List<LeadDtoGet> leads = leadService.getLeadsByUserId(userId);
        return ResponseEntity.ok(leads);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Object> updateLeadStatus(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @PathVariable("id") Integer id,
            @RequestBody LeadStatusRequestDto request) {
        if (request == null) {
            return ResponseEntity.badRequest().body("Status request body is required");
        }
        if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
            return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
        }
        UserDto user = jwtTokenGenerator.getUserFromJwtToken(authorizationHeader);
        try {
            LeadDtoGet updated = leadService.updateLeadStatus(
                    id, request.getStatus(), user.getId(), isAdministrator(user));
            return ResponseEntity.ok(updated);
        } catch (ResponseStatusException exception) {
            return statusError(exception);
        }
    }

    private boolean isAdministrator(UserDto user) {
        return Objects.equals(user.getRole(), "SUPER USER") || Objects.equals(user.getRole(), "ADMIN");
    }

    private ResponseEntity<Object> statusError(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
    }
}

