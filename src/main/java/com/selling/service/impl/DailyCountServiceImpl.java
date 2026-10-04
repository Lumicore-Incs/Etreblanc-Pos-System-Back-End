package com.selling.service.impl;

import com.selling.dto.PaginationResponse;
import com.selling.dto.get.DailyCountDetailsDtoGet;
import com.selling.dto.get.DailyCountDtoGet;
import com.selling.model.DailyCount;
import com.selling.model.DailyCountDetails;
import com.selling.repository.DailyCountRepo;
import com.selling.service.DailyCountService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DailyCountServiceImpl implements DailyCountService {

    private final DailyCountRepo dailyCountRepo;
    @Autowired
    private ModelMapper modelMapper;

    private DailyCountDtoGet convertToDto(DailyCount dailyCount) {
        DailyCountDtoGet map = modelMapper.map(dailyCount, DailyCountDtoGet.class);
        ArrayList<DailyCountDetailsDtoGet> objects = new ArrayList<>();
        for (DailyCountDetails dailyCountDetails : dailyCount.getDailyCountDetails()) {
            objects.add(modelMapper.map(dailyCountDetails, DailyCountDetailsDtoGet.class));
        }
        map.setDailyCountDetailsDtoGet(objects);
        return map;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<DailyCountDtoGet> getDailyCountByDatePaginated(int page, int size) {
        try {
            List<DailyCount> dailyCounts = dailyCountRepo.findByDate();
            
            long totalElements = dailyCounts.size();
            int totalPages = (int) Math.ceil((double) totalElements / size);
            
            // Validate page number
            if (page < 0) page = 0;
            if (page >= totalPages && totalElements > 0) page = totalPages - 1;
            
            int startIndex = Math.max(0, page * size);
            int endIndex = Math.min(startIndex + size, (int) totalElements);
            
            // Get paginated content
            List<DailyCountDtoGet> content = dailyCounts.subList(startIndex, endIndex).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            
            // Build response
            PaginationResponse<DailyCountDtoGet> response = new PaginationResponse<>();
            response.setContent(content);
            response.setPageNumber(page);
            response.setPageSize(size);
            response.setTotalElements(totalElements);
            response.setTotalPages(totalPages);
            response.setLastPage(page == totalPages - 1 || totalElements == 0);
            
            return response;
        } catch (Exception e) {
            System.out.println("Error fetching paginated daily counts: " + e.getMessage());
            throw new RuntimeException("Error fetching paginated daily counts", e);
        }
    }

}

