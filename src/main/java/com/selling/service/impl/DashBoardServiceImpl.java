package com.selling.service.impl;

import com.selling.dto.UserDto;
import com.selling.dto.get.ExcelTypeDto;
import com.selling.dto.get.WeeklyUserOrderDto;
import com.selling.model.*;
import com.selling.repository.*;
import com.selling.service.DashBoardService;
import com.selling.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;



@Service
@RequiredArgsConstructor
public class DashBoardServiceImpl implements DashBoardService {

    @Autowired
    private final CustomerRepo customerRepo;
    private final OrderRepo orderRepo;
    private final ProductRepo productRepo;
    private final DailyCountRepo dailyCountRepo;
    private final DailyCountDetailsRepo dailyCountDetailsRepo;
    @Autowired
    private StockService stockService;

//    @Override
//    public List<ExcelTypeDto> findOrder(String name) {
//
//        try {
//            List<ExcelTypeDto> excelTypeDtos = new ArrayList<>();
//
//            List<Order> pendingOrdersWithQuantities = customerRepo.findAllPendingOrdersWithQuantities();
//
//            if (pendingOrdersWithQuantities == null || name == null) {
//                return excelTypeDtos;
//            }
//
//            // ---- Mode detection ----
//            boolean isAll    = "all".equalsIgnoreCase(name.trim());
//            boolean isImport = "Import".equalsIgnoreCase(name.trim());
//
//            // ---- Product filter (only when a real product name is given) ----
//            Long filterProductId = null;
//            if (!isAll && !isImport) {
//                Product byName = productRepo.findByName(name);
//                if (byName == null) {
//                    // No such product -> nothing to export
//                    return excelTypeDtos;
//                }
//                filterProductId = Long.valueOf(byName.getProductId());
//            }
//
//            for (Order order : pendingOrdersWithQuantities) {
//
//                // ================= REMARK RULE =================
//                // Import        -> remark NOT null
//                // all           -> no remark filter
//                // product name  -> remark null only
//                boolean hasRemark = order.getRemark() != null && !order.getRemark().trim().isEmpty();
//
//                if (isImport) {
//                    if (!hasRemark) {
//                        continue;
//                    }
//                } else if (!isAll) {
//                    if (hasRemark) {
//                        continue;
//                    }
//                }
//
//                // ================= PRODUCT RULE =================
//                boolean isMatch = isAll || isImport;   // all / Import -> product filter නෑ
//                StringBuilder qtyDetails = new StringBuilder();
//
//                if (order.getOrderDetails() != null) {
//                    for (OrderDetails od : order.getOrderDetails()) {
//
//                        if (od.getProduct() == null) {
//                            continue;
//                        }
//
//                        Long orderProductId = Long.valueOf(od.getProduct().getProductId());
//
//                        // මේ detail line එක ගන්නද කියලා තනි තනිව බලනවා
//                        boolean detailMatch = isAll || isImport
//                                || (filterProductId != null && filterProductId.equals(orderProductId));
//
//                        if (detailMatch) {
//                            isMatch = true;
//                            qtyDetails.append(" + ")
//                                    .append(od.getProduct().getName())
//                                    .append(" + ")
//                                    .append(od.getQty());
//                        }
//                    }
//                }
//
//                if (!isMatch) {
//                    continue;
//                }
//
//                Customer customer = order.getCustomer();
//                if (customer == null) {
//                    continue;
//                }
//
//                ExcelTypeDto excelTypeDto = new ExcelTypeDto();
//                excelTypeDto.setId(order.getSerialNo());
//                excelTypeDto.setName(customer.getName());
//                excelTypeDto.setAddress(customer.getAddress());
//                excelTypeDto.setContact01(customer.getContact01());
//                excelTypeDto.setContact02(customer.getContact02());
//                excelTypeDto.setPrice(String.valueOf(order.getTotalPrice()));
//                excelTypeDto.setNote(order.getRemark());
//
//                // qtyDetails එකත් DTO එකට ඕන නම් මේක uncomment කරන්න
//                // excelTypeDto.setQty(qtyDetails.toString());
//
//                excelTypeDtos.add(excelTypeDto);
//            }
//
//            return excelTypeDtos;
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ArrayList<>();
//        }
//    }

    @Override
    public List<ExcelTypeDto> findOrder(String name) {

        try {
            List<ExcelTypeDto> excelTypeDtos = new ArrayList<>();

            List<Order> pendingOrdersWithQuantities = customerRepo.findAllPendingOrdersWithQuantities();

            Long filterProductId = null;

            // If name is not "all", get productId
            if (!name.equalsIgnoreCase("all")) {
                Product byName = productRepo.findByName(name);
                if (byName != null) {
                    filterProductId = Long.valueOf(byName.getProductId());
                }
            }

            for (Order order : pendingOrdersWithQuantities) {

                boolean isMatch = false;
                StringBuilder qtyDetails = new StringBuilder();

                for (OrderDetails od : order.getOrderDetails()) {

                    Long orderProductId = Long.valueOf(od.getProduct().getProductId());

                    // If "all", take all orders
                    if (name.equalsIgnoreCase("all")) {
                        isMatch = true;
                    }
                    // Otherwise match productId
                    else if (filterProductId != null && filterProductId.equals(orderProductId)) {
                        isMatch = true;
                    }

                    if (isMatch) {
                        qtyDetails.append(" + ")
                                .append(od.getProduct().getName())
                                .append(" + ")
                                .append(od.getQty());
                    }
                }

                if (isMatch) {
                    Customer customer = order.getCustomer();

                    ExcelTypeDto excelTypeDto = new ExcelTypeDto();
                    excelTypeDto.setId(order.getSerialNo());
                    excelTypeDto.setName(customer.getName());
                    excelTypeDto.setAddress(customer.getAddress());
                    excelTypeDto.setContact01(customer.getContact01());
                    excelTypeDto.setContact02(customer.getContact02());
                    excelTypeDto.setPrice(String.valueOf(order.getTotalPrice()));
                    excelTypeDto.setNote(order.getRemark());

                    excelTypeDtos.add(excelTypeDto);
                }
            }

            return excelTypeDtos;

        } catch (Exception e) {
            return null;
        }
    }


    @Transactional
    @Override
    public String ConformOrder(List<String> serialNumbers) {
        try {
            LocalDate today = LocalDate.now();
            Date todayDate = Date.valueOf(today);

            for (String serialNumber : serialNumbers) {
                System.out.println("serial Number :"+serialNumber);
                Optional<Order> bySerialNo = orderRepo.findBySerialNo(serialNumber);
                if (bySerialNo.isPresent()) {
                    Order order = bySerialNo.get();
                    order.getCustomer().setStatus("PRINTING");
                    customerRepo.save(order.getCustomer());
                    // Get or create DailyCount for today
                    DailyCount dailyCount = dailyCountRepo.findByDate(todayDate).orElse(null);

                    // If no DailyCount exists for today, create one
                    if (dailyCount == null) {
                        dailyCount = new DailyCount();
                        dailyCount.setDate(todayDate);
                        dailyCount.setLastTime(LocalDateTime.now());
                        dailyCount.setTotalQty(0);
                        dailyCount = dailyCountRepo.save(dailyCount);
                    }

                    // Process each order detail
                    List<OrderDetails> orderDetailsList = order.getOrderDetails();
                    if (orderDetailsList != null && !orderDetailsList.isEmpty()) {
                        for (OrderDetails orderDetail : orderDetailsList) {

                            Product product = orderDetail.getProduct();

                            stockService.updateStockByName(product.getName(), orderDetail.getQty());

                            Integer productId = product.getProductId();
                            String productName = product.getName();
                            Integer qty = orderDetail.getQty();

                            Optional<DailyCountDetails> existingDetails = dailyCountDetailsRepo
                                    .findByDailyCountAndProductIdAndCategory(dailyCount, productId,qty);

                            if (existingDetails.isPresent()){
                                DailyCountDetails dailyCountDetails = existingDetails.get();
                                dailyCountDetails.setQty(dailyCountDetails.getQty()+1);
                                dailyCountDetailsRepo.save(dailyCountDetails);
                            }else {
                                DailyCountDetails newDetails = new DailyCountDetails();
                                newDetails.setProductId(productId);
                                newDetails.setProductName(productName);
                                newDetails.setQty(1);
                                newDetails.setCategory(qty);
                                newDetails.setDailyCount(dailyCount);
                                newDetails.setProduct(product);
                                dailyCountDetailsRepo.save(newDetails);
                            }

                            // Update total quantity in DailyCount
                            dailyCount.setTotalQty(dailyCount.getTotalQty() + 1);
                        }

                        dailyCount.setLastTime(LocalDateTime.now());
                        dailyCountRepo.save(dailyCount);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("message is : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return "success";
    }

    @Override
    public int getTotalOrder(UserDto user) {

        LocalDate today = LocalDate.now();

        LocalDate startDate;

        if (today.getDayOfMonth() >= 15) {
            // This month 15
            startDate = LocalDate.of(today.getYear(), today.getMonth(), 15);
        } else {
            // Previous month 15
            LocalDate previousMonth = today.minusMonths(1);
            startDate = LocalDate.of(previousMonth.getYear(), previousMonth.getMonth(), 15);
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();

        if (user.getRole().equals("SUPER USER") || user.getRole().equals("ADMIN")) {

            return Math.toIntExact(orderRepo.sumQtyAfterDate(startDateTime));
        }

        return Math.toIntExact(orderRepo.sumQtyByUserAfterDate(
                user.getId(),
                startDateTime
        ));
    }


    @Override
    public int getTodayOrder(UserDto user) {

        LocalDate today = LocalDate.now();

        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(23, 59, 59);

        if (user.getRole().equals("SUPER USER") || user.getRole().equals("ADMIN")) {
            return orderRepo.sumTodayQty(start, end).intValue();
        }

        return orderRepo.sumTodayQtyByUser(
                user.getId(),
                start,
                end
        ).intValue();
    }


    @Override
    public int getConformOrder(UserDto user) {

        LocalDate today = LocalDate.now();

        LocalDate startDate;

        if (today.getDayOfMonth() >= 15) {
            // This month 15
            startDate = LocalDate.of(today.getYear(), today.getMonth(), 15);
        } else {
            // Previous month 15
            LocalDate previousMonth = today.minusMonths(1);
            startDate = LocalDate.of(previousMonth.getYear(), previousMonth.getMonth(), 15);
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = LocalDateTime.now();

        if (user.getRole().equals("SUPER USER") || user.getRole().equals("ADMIN")) {
            return Math.toIntExact(orderRepo.countByStatusAndDateRange("Delivered", startDateTime, endDateTime));
        }

        return Math.toIntExact(orderRepo.countByCustomerUserIdAndStatusAndDateBetween(
                user.getId(), "Delivered", startDateTime, endDateTime));
    }

//    @Override
//    public int getCancelOrder(UserDto user) {
//
//        LocalDate today = LocalDate.now();
//        String cancelStatus = "Returned to Client";
//
//        LocalDate startDate;
//
//        if (today.getDayOfMonth() >= 15) {
//            // This month 15
//            startDate = LocalDate.of(today.getYear(), today.getMonth(), 15);
//        } else {
//            // Previous month 15
//            LocalDate previousMonth = today.minusMonths(1);
//            startDate = LocalDate.of(previousMonth.getYear(), previousMonth.getMonth(), 15);
//        }
//
//        LocalDateTime startDateTime = startDate.atStartOfDay();
//        LocalDateTime endDateTime = LocalDateTime.now();
//
//        if (user.getRole().equals("SUPER USER") || user.getRole().equals("ADMIN")) {
//            return Math.toIntExact(orderRepo.countByStatusAndDateRange(cancelStatus, startDateTime, endDateTime));
//        }
//
//        return Math.toIntExact(orderRepo.countByCustomerUserIdAndStatusAndDateBetween(
//                user.getId(), cancelStatus, startDateTime, endDateTime));
//    }

    @Override
    public int getCancelOrder(UserDto user) {

        List<String> returnStatuses = Arrays.asList(
                "FAILED TO DELIVER",
                "RETURNED TO BRANCH FAILED",
                "RETURNED TO HO",
                "COLLECT FROM RETURN SHUTTLE",
                "COLLECT FROM WAREHOUSE (RETURN ORDER)",
                "RECEIVED AT HO (RETURNED ITEM)",
                "RETURNED TO CLIENT",
                "RECEIVED BY CLIENT"
        );

        LocalDate today = LocalDate.now();

        LocalDate startDate;

        if (today.getDayOfMonth() >= 15) {
            // This month 15
            startDate = LocalDate.of(today.getYear(), today.getMonth(), 15);
        } else {
            // Previous month 15
            LocalDate previousMonth = today.minusMonths(1);
            startDate = LocalDate.of(previousMonth.getYear(), previousMonth.getMonth(), 15);
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = LocalDateTime.now();

        if (user.getRole().equals("SUPER USER") || user.getRole().equals("ADMIN")) {
            return Math.toIntExact(
                    orderRepo.countByStatusInAndDateBetween(
                            returnStatuses,
                            startDateTime,
                            endDateTime
                    )
            );
        }

        return Math.toIntExact(
                orderRepo.countByCustomerUserIdAndStatusInAndDateBetween(
                        user.getId(),
                        returnStatuses,
                        startDateTime,
                        endDateTime
                )
        );
    }


    @Override
    public int getConformOrderByUser(UserDto user) {

        LocalDate today = LocalDate.now();

        LocalDate startDate;

        if (today.getDayOfMonth() >= 15) {
            // This month 15
            startDate = LocalDate.of(today.getYear(), today.getMonth(), 15);
        } else {
            // Previous month 15
            LocalDate previousMonth = today.minusMonths(1);
            startDate = LocalDate.of(previousMonth.getYear(), previousMonth.getMonth(), 15);
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = LocalDateTime.now();

        long resultCount = orderRepo.countByCustomerUserIdAndStatusAndDateBetween(
                user.getId(), "Delivered", startDateTime, endDateTime);

        return Math.toIntExact(resultCount);
    }

    @Override
    public int getCancelOrderByUser(UserDto user) {

        LocalDate today = LocalDate.now();
        String cancelStatus = "Returned to Client";

        LocalDate startDate;

        if (today.getDayOfMonth() >= 15) {
            // This month 15
            startDate = LocalDate.of(today.getYear(), today.getMonth(), 15);
        } else {
            // Previous month 15
            LocalDate previousMonth = today.minusMonths(1);
            startDate = LocalDate.of(previousMonth.getYear(), previousMonth.getMonth(), 15);
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = LocalDateTime.now();

        return Math.toIntExact(orderRepo.countByCustomerUserIdAndStatusAndDateBetween(
                user.getId(), cancelStatus, startDateTime, endDateTime));
    }

    @Override
    public List<Object> findOrderQty() {
        try {

            List<Order> pendingOrders = customerRepo.findAllPendingOrdersWithQuantities();

            Map<String, Long> productOrderCount = pendingOrders.stream()
                    .flatMap(order -> order.getOrderDetails().stream()
                            .map(od -> new AbstractMap.SimpleEntry<>(
                                    od.getProduct().getName()
                                            + " " + od.getQty(),
                                    order.getOrderId()
                            ))
                    )
                    .distinct() // 🔥 remove duplicate same order-product
                    .collect(Collectors.groupingBy(
                            Map.Entry::getKey,
                            Collectors.counting() // ✅ COUNT ORDERS
                    ));

            List<Object> result = productOrderCount.entrySet().stream()
                    .map(entry -> {
                        Map<String, Object> obj = new HashMap<>();
                        obj.put("productName", entry.getKey());
                        obj.put("totalQty", entry.getValue()); // actually order count
                        return obj;
                    })
                    .collect(Collectors.toList());

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public int processingOrders(UserDto user) {
        LocalDate today = LocalDate.now();

        LocalDateTime startDateTime = today.atStartOfDay();
        LocalDateTime endDateTime = today.plusDays(1).atStartOfDay();

        if (user.getRole().equals("SUPER USER") || user.getRole().equals("ADMIN")) {
            return Math.toIntExact(orderRepo.countByProcessingOrdersAndDateRange("Processing", startDateTime, endDateTime));
        }

        return Math.toIntExact(orderRepo.countByCustomerUserIdAndProcessingOrdersAndDateBetween(
                user.getId(), "Processing", startDateTime, endDateTime));
    }

    @Override
    public Map<String, Integer> DailyUsersOrders(LocalDate date) {

        List<Object[]> results = orderRepo.getDailyUserOrders(date);

        Map<String, Integer> userOrders = new HashMap<>();

        for (Object[] row : results) {

            String userName = (String) row[0];
            Integer qty = ((Number) row[1]).intValue();

            userOrders.put(userName, qty);
        }

        return userOrders;
    }

    @Override
    public List<WeeklyUserOrderDto> findWeeklyOrder() {

        LocalDate today = LocalDate.now();

        // Monday
        LocalDate startOfWeek =
                today.with(DayOfWeek.MONDAY);

        // Next Monday
        LocalDate endOfWeek =
                startOfWeek.plusDays(7);

        LocalDateTime startDate =
                startOfWeek.atStartOfDay();

        LocalDateTime endDate =
                endOfWeek.atStartOfDay();

        List<Object[]> results =
                orderRepo.findWeeklyUserOrders(
                        startDate,
                        endDate
                );

        Map<LocalDate, Map<String, Integer>> grouped =
                new LinkedHashMap<>();

        for (Object[] row : results) {

            LocalDate date = ((Date) row[0]).toLocalDate();

            String userName = (String) row[1];

            Integer qty = ((Number) row[2]).intValue();

            grouped
                    .computeIfAbsent(date, k -> new LinkedHashMap<>())
                    .put(userName, qty);
        }

        List<WeeklyUserOrderDto> response = new ArrayList<>();

        for (Map.Entry<LocalDate, Map<String, Integer>> entry
                : grouped.entrySet()) {

            response.add(
                    new WeeklyUserOrderDto(
                            entry.getKey().getDayOfWeek().toString(),
                            entry.getValue()
                    )
            );
        }

        return response;
    }
}