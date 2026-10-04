package com.selling.service.impl;

import com.selling.dto.get.UserSummeryDetailsGet;
import com.selling.dto.get.UserSummeryGet;
import com.selling.model.Order;
import com.selling.model.OrderDetails;
import com.selling.model.Payment;
import com.selling.repository.OrderRepo;
import com.selling.repository.PaymentRepo;
import com.selling.service.SummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SummaryServiceImpl implements SummaryService {

    private final OrderRepo orderRepo;
    private final PaymentRepo paymentRepo;

    @Override
    public UserSummeryGet getSummaryDetails(Integer id, Integer month) {

        // Calculate date range: 15th of (month-1) to 15th of (month)
        int currentYear = LocalDate.now().getYear();

        // Handle year transition if month is 1 (January)
        int prevMonth = month == 1 ? 12 : month - 1;
        int prevYear = month == 1 ? currentYear - 1 : currentYear;

        LocalDateTime startDate = LocalDateTime.of(prevYear, prevMonth, 15, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(currentYear, month, 15, 23, 59, 59);

        List<Order> orders = orderRepo.findByUserIdAndDateBetweenByStatus((long) id, startDate, endDate);
        List<Order> ordersList = orderRepo.findByUserIdAndDateBetween((long) id, startDate, endDate);

        int totalOrders = ordersList.size();
        int totalQty = 0;
        int deliveredCount = 0;
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (Order order : ordersList) {
             int orderQty = 0;
            if (order.getOrderDetails() != null) {
                for (OrderDetails od : order.getOrderDetails()) {
                    orderQty += (od.getQty() != null ? od.getQty() : 0);
                }
            }
            
            totalQty += orderQty;
        }

        Map<LocalDate, UserSummeryDetailsGet> groupedDetails = new HashMap<>();

        for (Order order : orders) {
            int orderQty = 0;
            if (order.getOrderDetails() != null) {
                for (OrderDetails od : order.getOrderDetails()) {
                    orderQty += (od.getQty() != null ? od.getQty() : 0);
                }
            }
            
            deliveredCount += orderQty;
//            totalPrice = totalPrice.add(order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO);
            
            LocalDate orderDate = order.getDeliveryDate().toLocalDate();
            BigDecimal orderTotal = order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO;

            if (groupedDetails.containsKey(orderDate)) {
                UserSummeryDetailsGet existing = groupedDetails.get(orderDate);
                existing.setQty(existing.getQty() + orderQty);
                BigDecimal currentTotal = existing.getTotal() != null ? existing.getTotal() : BigDecimal.ZERO;
                existing.setTotal(currentTotal.add(orderTotal));
            } else {
                UserSummeryDetailsGet detail = new UserSummeryDetailsGet();
                Optional<Payment> byUserId = paymentRepo.findByUserId(order.getUser().getId());
                // Set to start of day for consistency
                detail.setDate(orderDate.atStartOfDay());
                detail.setQty(orderQty);
                detail.setCommission(BigDecimal.ZERO);
                detail.setTotal(BigDecimal.ZERO);
                if (byUserId.isPresent()) {
                    detail.setCommission(BigDecimal.valueOf(byUserId.get().getCommission()));
                    detail.setTotal(BigDecimal.valueOf(byUserId.get().getCommission()*orderQty));
                }

                groupedDetails.put(orderDate, detail);
            }
        }

        List<UserSummeryDetailsGet> detailsList = new ArrayList<>(groupedDetails.values());
        // Sort by date ascending
        detailsList.sort(Comparator.comparing(UserSummeryDetailsGet::getDate));

        UserSummeryGet summary = new UserSummeryGet();
        summary.setTotalOrders(totalOrders);
        summary.setTotalItem(totalQty);
        summary.setDeleverd(deliveredCount);
        summary.setTotal(totalPrice);
        summary.setSummery(detailsList);

        return summary;
    }
}
