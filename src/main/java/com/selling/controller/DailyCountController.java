package com.selling.controller;

import com.selling.dto.PaginationResponse;
import com.selling.dto.get.DailyCountDtoGet;
import com.selling.service.DailyCountService;
import com.selling.util.JWTTokenGenerator;
import com.selling.util.TokenStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin()
@RestController
@RequiredArgsConstructor
@RequestMapping("/report")
public class DailyCountController {

    private final DailyCountService dailyCountService;
    private final JWTTokenGenerator jwtTokenGenerator;
//
//    @GetMapping("/daily-count")
//    public ResponseEntity<Object> getDailyCountByDate(
//            @RequestHeader(name = "Authorization") String authorizationHeader,
//            @RequestParam String date) {
//        try {
//            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
//                return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
//            }
//
//            Date sqlDate = Date.valueOf(date);
//            List<DailyCountDto> dailyCounts = dailyCountService.getDailyCountByDate(sqlDate);
//            return new ResponseEntity<>(dailyCounts, HttpStatus.OK);
//
//        } catch (Exception e) {
//            return new ResponseEntity<>("Error fetching daily counts: " + e.getMessage(),
//                    HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    /**
     * Get daily counts with pagination (10 records per page)
     * Usage: /report/daily-count/paginated?date=2026-02-26&page=0&size=10
     */
    @GetMapping("/daily-count/paginated")
    public ResponseEntity<Object> getDailyCountByDatePaginated(
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        try {
            if (!jwtTokenGenerator.validateJwtToken(authorizationHeader)) {
                return new ResponseEntity<>(TokenStatus.TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
            }

            PaginationResponse<DailyCountDtoGet> response = dailyCountService.getDailyCountByDatePaginated(page, size);
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            return new ResponseEntity<>("Error fetching paginated daily counts: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

