package com.selling.service.impl;

import static com.selling.dto.ApiResponse.success;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.selling.dto.*;
import com.selling.model.*;
import com.selling.repository.CustomerRepo;
import com.selling.service.StockService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.selling.dto.get.OrderDetailsDtoGet;
import com.selling.dto.get.OrderDtoGet;
import com.selling.repository.OrderDetailsRepo;
import com.selling.repository.OrderRepo;
import com.selling.repository.ProductRepo;
import com.selling.service.OrderService;
import com.selling.util.MapperService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepo orderRepo;
    private final OrderDetailsRepo orderDetailsRepo;
    private final MapperService mapperService;
    private final ProductRepo productRepository;
    private final StockService stockService;
    private final CustomerRepo customerRepo;


    @Override
    public String generateOrderSerialNumber(Product product) {
        // Global increment: fetch most recent order, parse its numeric suffix,
        // increment
        Order last = orderRepo.findTopBySerialNoStartingWithOrderBySerialNoDesc(product.getSerialPrefix());
        long nextNum = 1L;
        if (last != null && last.getSerialNo() != null) {
            String serial = last.getSerialNo();
            // numeric suffix is trailing digits; find last non-digit
            int i = serial.length() - 1;
            while (i >= 0 && Character.isDigit(serial.charAt(i)))
                i--;
            String numPart = (i < serial.length() - 1) ? serial.substring(i + 1) : null;
            try {
                if (numPart != null)
                    nextNum = Long.parseLong(numPart) + 1L;
            } catch (NumberFormatException ignored) {
                nextNum = 1L;
            }
        }

        String prefix = product.getSerialPrefix() != null ? product.getSerialPrefix() : "XXX";
        // format number as 5 digits with leading zeros
        String numFormatted = String.format("%05d", nextNum);
        return prefix + numFormatted;
    }

    @Override
    @Transactional
    public String trackingUpload(List<TrackingDto> trackingList) {
        try {
            List<String> results = new ArrayList<>();
            int successCount = 0;
            int failureCount = 0;

            for (TrackingDto trackingDto : trackingList) {
                String result = updateTrackingForOrder(trackingDto);
                results.add(result);

                if (result.startsWith("Success")) {
                    successCount++;
                } else {
                    failureCount++;
                }
            }

            return String.format("Completed: %d successful, %d failed. Details: %s",
                    successCount, failureCount, String.join("; ", results));

        } catch (Exception e) {
            return "Error processing tracking upload: " + e.getMessage();
        }
    }

    private String updateTrackingForOrder(TrackingDto trackingDto) {
        try {
            // serialNo (orderId in DTO) මගින් order එක සොයාගැනීම
            String serialNo = trackingDto.getOrderId();
            String wayBillNo = trackingDto.getWayBillNo();

            if (serialNo == null || serialNo.trim().isEmpty()) {
                return "Failed: Serial number is empty for waybill: " + wayBillNo;
            }

            if (wayBillNo == null || wayBillNo.trim().isEmpty()) {
                return "Failed: Waybill number is empty for order: " + serialNo;
            }

            // Order එක සොයාගැනීම
            Order order = orderRepo.findBySerialNo(serialNo)
                    .orElse(null);

            if (order == null) {
                return "Failed: Order not found with serial number: " + serialNo;
            }

            // trackingId update කිරීම
            order.setTrackingId(wayBillNo);
            orderRepo.save(order);

            return String.format("Success: Order %s updated with tracking ID %s",
                    serialNo, wayBillNo);

        } catch (Exception e) {
            return "Failed: Error updating order " + trackingDto.getOrderId() + " - " + e.getMessage();
        }
    }

    // private final VonageClient vonageClient;

    @Value("${vonage.sms.sender}")
    private String senderId;

    @Override
    public List<OrderDtoGet> getAllTodayOrder() {
        List<OrderDtoGet> customerDtoGetList = new ArrayList<>();
        List<Order> allCustomer = orderRepo.findAll();

        // Get today's date (without time)
        LocalDate today = LocalDate.now();

        for (Order order : allCustomer) {
            // Convert order date to LocalDate (if stored as LocalDateTime)
            LocalDate orderDate = order.getDate().toLocalDate();

            // Check if year, month, and day match
            if (orderDate.equals(today)) {
                if (order.getCustomer() != null) {
                    OrderDtoGet map = mapperService.map(order, OrderDtoGet.class);
                    map.setCustomer(mapperService.map(order.getCustomer(), CustomerDto.class));
                    map.setOrderDetails(getOrderDetailsData(order));
                    customerDtoGetList.add(map);
                }
            }
        }
        return customerDtoGetList;
    }

    @Override
    public List<OrderDtoGet> getAllOrder() {
        List<OrderDtoGet> customerDtoGetList = new ArrayList<>();
        List<Order> allCustomer = orderRepo.findAll();

        for (Order order : allCustomer) {
            if (order.getCustomer() != null) {
                OrderDtoGet map = mapperService.map(order, OrderDtoGet.class);
                map.setCustomer(mapperService.map(order.getCustomer(), CustomerDto.class));
                map.setOrderDetails(getOrderDetailsData(order));
                customerDtoGetList.add(map);
            }
        }
        return customerDtoGetList;
    }

    @Override
    public List<OrderDtoGet> getAllTodayOrderByUserId(UserDto userDto) {
        List<OrderDtoGet> orderDtoGetList = new ArrayList<>();

        LocalDate today = LocalDate.now();
        List<Order> userOrders = orderRepo
                .findByUser((userDto == null) ? null : mapperService.map(userDto, User.class));

        for (Order order : userOrders) {
            LocalDate orderDate = order.getDate().toLocalDate();

            if (orderDate.equals(today)) {
                if (order.getCustomer() != null) {
                    OrderDtoGet map = mapperService.map(order, OrderDtoGet.class);
                    map.setCustomer(mapperService.map(order.getCustomer(), CustomerDto.class));
                    map.setOrderDetails(getOrderDetailsData(order));
                    orderDtoGetList.add(map);
                }
            }
        }
        return orderDtoGetList;
    }

    @Override
    public List<OrderDtoGet> getAllOrderByUserId(UserDto userDto) {
        List<OrderDtoGet> orderDtoGetList = new ArrayList<>();
        List<Order> userOrders = orderRepo
                .findByUser((userDto == null) ? null : mapperService.map(userDto, User.class));
        for (Order order : userOrders) {
            if (order.getCustomer() != null) {
                OrderDtoGet map = mapperService.map(order, OrderDtoGet.class);
                map.setCustomer(mapperService.map(order.getCustomer(), CustomerDto.class));
                map.setOrderDetails(getOrderDetailsData(order));
                orderDtoGetList.add(map);
            }
        }
        return orderDtoGetList;
    }

    @Override
    public void updateOrderDetails() {
        List<Order> recentOrders = orderRepo.findTop200ByOrderByOrderIdDesc();
        for (Order order : recentOrders) {
            if (!(order.getStatus().equals("Delivered") || order.getStatus().equals("Failed to Deliver")
                    || order.getStatus().equals("NotFound"))) {
                String value = checkTrackingStatus(order.getTrackingId());
                // Only update if we got a valid status (not null due to API failure)
                if (value != null && !value.equals(order.getStatus())) {
                    order.setStatus(value);
                    // sendMassage("0782862763", value);
                    orderRepo.save(order);
                }
            }
        }
    }

    private String checkTrackingStatus(String id) {
        String apiUrl = "https://api.transexpress.lk/api/v1/tracking?waybill_id=" + id;
        HttpURLConnection connection = null;

        try {
            URL url = new URL(apiUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                JsonObject jsonResponse = JsonParser.parseReader(in).getAsJsonObject();
                in.close();

                JsonArray dataArray = jsonResponse.getAsJsonArray("data");
                String lastStatus = null;

                for (int i = 0; i < dataArray.size(); i++) {
                    JsonObject dataItem = dataArray.get(i).getAsJsonObject();
                    if ("tracking_history".equals(dataItem.get("key").getAsString())) {
                        JsonArray historyArray = dataItem.getAsJsonArray("value");

                        if (historyArray.size() > 0) {
                            JsonObject lastStatusItem = historyArray.get(historyArray.size() - 1).getAsJsonObject();
                            lastStatus = lastStatusItem.get("status_name").getAsString();
                        }
                        break;
                    }
                }

                if (lastStatus != null) {
                    return lastStatus;
                } else {
                    // Don't return "NotFound" - preserve existing status when API has no data
                    System.out.println("No tracking history found for ID: " + id);
                    return null; // Return null to indicate "no update needed"
                }
            } else {
                System.out.println("API request failed with response code: " + responseCode + " for tracking ID: " + id);
                return null; // Don't update status on API failure
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error while calling API for tracking ID " + id + ": " + e.getMessage());
            return null; // Don't update status on API exception
        } finally {
            // Clean up connection if it was created
            if (connection != null) {
                try {
                    connection.disconnect();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private List<OrderDetailsDtoGet> getOrderDetailsData(Order orderId) {
        List<OrderDetails> byOrder = orderDetailsRepo.findByOrder(orderId);
        List<OrderDetailsDtoGet> orderDetailsDtoGetList = new ArrayList<>();
        for (OrderDetails orderDetails : byOrder) {
            if (orderDetails != null) {
                OrderDetailsDtoGet map = mapperService.map(orderDetails, OrderDetailsDtoGet.class);
                map.setProductId((orderDetails.getProduct() == null) ? null
                        : mapperService.map(orderDetails.getProduct(), ProductDto.class));
                orderDetailsDtoGetList.add(map);
            }
        }
        return orderDetailsDtoGetList;
    }

    @Override
    public List<OrderDtoGet> getTemporaryOrders(UserDto userDto) {
        List<OrderDtoGet> orderDtoGetList = new ArrayList<>();
        List<Order> temporaryOrders = orderRepo.findByStatus("TEMPORARY");

        if (Objects.equals(userDto.getRole(), "admin") || Objects.equals(userDto.getRole(), "ADMIN") || Objects.equals(userDto.getRole(), "SUPER USER") || Objects.equals(userDto.getRole(), "SUPERUSER")) {
            for (Order order : temporaryOrders) {
                OrderDtoGet dto = mapperService.map(order, OrderDtoGet.class);
                dto.setOrderDetails(getOrderDetailsData(order));
                if (order.getCustomer() != null) {
                    dto.setCustomer(mapperService.map(order.getCustomer(), CustomerDto.class));
                }
                orderDtoGetList.add(dto);
            }
        } else {
            for (Order order : temporaryOrders) {
                OrderDtoGet dto = mapperService.map(order, OrderDtoGet.class);
                if (order.getCustomer() != null) {
                    dto.setCustomer(mapperService.map(order.getCustomer(), CustomerDto.class));
                    dto.setOrderDetails(getOrderDetailsData(order));
                }
                if (order.getCustomer().getUser().getId().equals(userDto.getId())) {
                    orderDtoGetList.add(dto);
                }
            }
        }
        return orderDtoGetList;
    }

    // here we have added order update flow to this same method
    @Override
    public Object resolveDuplicateOrder(Integer orderId, String userRole, CustomerRequestDTO requestDTO) {
        try {
            Order order = orderRepo.findById(orderId).orElse(null);
            if (order == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
            }

            // Only resolve if status is TEMPORARY or similar
            if ("TEMPORARY".equals(order.getStatus())) {
                order.setStatus("PENDING");
            }
            order.setTotalPrice(requestDTO.getTotalPrice());
            order.setRemark(requestDTO.getRemark());
            Order savedOrder = orderRepo.save(order);

            // 3. Save Order Details
            List<OrderDetails> orderDetailsList = requestDTO.getItems().stream()
                    .map(item -> {
                        Product product = productRepository.findAllByProductId(item.getProductId());
                        if (product == null) {
                            throw new ResponseStatusException(
                                    HttpStatus.NOT_FOUND, "Product not found with id: " + item.getProductId());
                        }

                        OrderDetails orderDetails = new OrderDetails();
                        orderDetails.setOrderDetailsId(item.getOrderDetailsId());
                        orderDetails.setOrder(savedOrder);
                        orderDetails.setProduct(product);
                        orderDetails.setQty(item.getQty());
                        orderDetails.setTotal(item.getTotal());

                        return orderDetails;
                    })
                    .collect(Collectors.toList());

            orderDetailsRepo.saveAll(orderDetailsList);

            OrderDtoGet dto = mapperService.map(order, OrderDtoGet.class);
            if (order.getCustomer() != null) {
                dto.setCustomer(mapperService.map(order.getCustomer(), CustomerDto.class));
            }
            dto.setOrderDetails(getOrderDetailsData(order));
            return dto;
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error resolving order: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Object deleteOrder(Integer orderId) {
        try {
            Order order = orderRepo.findById(orderId).orElse(null);
            if (order == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
            }
            Customer customer = order.getCustomer();

            if (order.getStatus().equals("PENDING") || order.getStatus().equals("TEMPORARY")) {
                List<OrderDetails> details = orderDetailsRepo.findByOrder(order);
                if (details != null && !details.isEmpty()) {
                    orderDetailsRepo.deleteAll(details);
                }

                orderRepo.delete(order);
                stockService.updateStockQty(details);

                Order customerOtherOrder = orderRepo.findByCustomer(order.getCustomer());
                if (customerOtherOrder == null) {
                    customerRepo.delete(customer);
                }

                return success("Order deleted successfully", null);
            }
            return null;

        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting order: " + e.getMessage());
        }
    }

}
