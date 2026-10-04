package com.selling.service.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.selling.dto.*;
import com.selling.dto.get.MonthlyOrderReportDtoGet;
import com.selling.dto.get.OrderDetailsDtoGet;
import com.selling.dto.get.OrderDtoGet;
import com.selling.model.*;
import com.selling.repository.*;
import com.selling.service.OrderService;
import com.selling.util.MapperService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.selling.dto.ApiResponse.success;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepo orderRepo;
    private final OrderDetailsRepo orderDetailsRepo;
    private final MapperService mapperService;
    private final ProductRepo productRepository;
    private final CustomerRepo customerRepo;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    @Qualifier("orderExecutor")
    private Executor orderExecutor;
    @Autowired
    private UserRepo userRepo;

    @Override
    public String generateOrderSerialNumber(Product product, UserDto userDto) {
        Order last = orderRepo.findTopBySerialNoStartingWithOrderBySerialNoDesc(userDto.getSerialPrefix());

        long nextNum = 1L;
        if (last != null && last.getSerialNo() != null) {
            String serial = last.getSerialNo();
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

        String prefix = userDto.getSerialPrefix() != null ? userDto.getSerialPrefix() : "XXX";
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

    @Override
    public ArrayList<String> getUrgentOrders(Long id, String date) {
        return null;
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
            order.setWeyBillId(wayBillNo);
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
    public List<MonthlyOrderReportDtoGet> getAllOrderByUserId(
            long userId,
            LocalDate date,
            YearMonth month
    ) {

        List<MonthlyOrderReportDtoGet> orderDtoGetList = new ArrayList<>();

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Order> userOrders;

        if (date != null) {
            userOrders = orderRepo.findByUserAndDate(user, date);
        } else if (month != null) {
            userOrders = orderRepo.findByUserAndMonth(
                    user,
                    month.getYear(),
                    month.getMonthValue()
            );
        } else {
            throw new RuntimeException("Date or Month required");
        }

        Map<LocalDate, MonthlyOrderReportDtoGet> reportMap = new LinkedHashMap<>();

        for (Order order : userOrders) {

            if (order == null || order.getCustomer() == null) continue;

            if (order.getDate() == null) continue; // 🔥 FIX 1

            LocalDate orderDate = order.getDate().toLocalDate();

            MonthlyOrderReportDtoGet dto = reportMap.getOrDefault(
                    orderDate,
                    new MonthlyOrderReportDtoGet(
                            Date.valueOf(orderDate),
                            0,
                            0
                    )
            );

            dto.setTotalOrders(dto.getTotalOrders() + 1);

            int totalItems = 0;

            if (order.getOrderDetails() != null) {
                totalItems = order.getOrderDetails()
                        .stream()
                        .filter(Objects::nonNull)
                        .mapToInt(od -> od.getQty() == null ? 0 : od.getQty())
                        .sum();
            }

            dto.setTotalItems(dto.getTotalItems() + totalItems);

            reportMap.put(orderDate, dto);
        }

        return new ArrayList<>(reportMap.values());
    }

    @Override
    public PaginationResponse<OrderDtoGet> getAllTodayOrderPaginated(int page, int size, String search, String status,
                                                                     Integer productId) {
        List<Order> allOrders = orderRepo.findAll();
        return buildFilteredPaginatedResponse(allOrders, page, size, search, status, productId, null, true, null);
    }

    @Override
    public PaginationResponse<OrderDtoGet> getAllTodayOrderByUserIdPaginated(UserDto userDto, int page, int size,
                                                                             String search, String status, Integer productId) {
        List<Order> userOrders = orderRepo.findByUser(mapperService.map(userDto, User.class));
        return buildFilteredPaginatedResponse(userOrders, page, size, search, status, productId, null, true, null);
    }

    @Override
    public PaginationResponse<OrderDtoGet> getAllOrderPaginated(int page, int size, String search, String status,
                                                                Integer productId, Date date) {
        List<Order> allOrders = orderRepo.findAllByOrderByOrderIdDesc();
        return buildFilteredPaginatedResponse(allOrders, page, size, search, status, productId, date, false, null);
    }

    @Override
    public PaginationResponse<OrderDtoGet> getAllOrderByUserIdPaginated(UserDto userDto, int page, int size,
                                                                        String search, String status, Integer productId, Date date) {
        List<Order> userOrders = orderRepo.findByUserOrderByOrderIdDesc(mapperService.map(userDto, User.class));
        return buildFilteredPaginatedResponse(userOrders, page, size, search, status, productId, date, false, null);
    }


    private PaginationResponse<OrderDtoGet> buildFilteredPaginatedResponse(
            List<Order> source,
            int page,
            int size,
            String search,
            String status,
            Integer productId,
            Date date,
            boolean todayOnly,
            Object ignored) {

        try {

            // Resolve order IDs that contain the requested product
            final Set<Integer> productOrderIds;

            if (productId != null) {
                productOrderIds = new HashSet<>(orderRepo.findOrderIdsByProductId(productId));
            } else {
                productOrderIds = null;
            }

            LocalDate today = LocalDate.now();

            String searchLower =
                    (search != null && !search.trim().isEmpty())
                            ? search.toLowerCase().trim()
                            : null;

            String statusFilter =
                    (status != null
                            && !status.trim().isEmpty()
                            && !status.equalsIgnoreCase("ALL STATUS"))
                            ? status.trim()
                            : null;

            // Convert java.util.Date -> LocalDate
            LocalDate filterDate = null;

            if (date != null) {
                filterDate = date.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
            }

            LocalDate finalFilterDate = filterDate;

            List<Order> filtered = source.stream()
                    .filter(order -> {

                        if (order == null)
                            return false;

                        if (order.getCustomer() == null)
                            return false;

                        if (order.getDate() == null)
                            return false;

                        LocalDate orderDate = order.getDate().toLocalDate();

                        // Today only filter
                        if (todayOnly && !orderDate.equals(today)) {
                            return false;
                        }

                        // Date filter
                        if (finalFilterDate != null
                                && !orderDate.equals(finalFilterDate)) {
                            return false;
                        }

                        // Status filter
                        if (statusFilter != null
                                && !statusFilter.equalsIgnoreCase(order.getStatus())) {
                            return false;
                        }

                        // Product filter
                        if (productOrderIds != null
                                && !productOrderIds.contains(order.getOrderId())) {
                            return false;
                        }

                        // Search filter
                        if (searchLower != null) {

                            String customerName =
                                    order.getCustomer().getName() != null
                                            ? order.getCustomer().getName().toLowerCase()
                                            : "";

                            String waybill =
                                    order.getWeyBillId() != null
                                            ? order.getWeyBillId().toLowerCase()
                                            : "";

                            String contact01 =
                                    order.getCustomer().getContact01() != null
                                            ? order.getCustomer().getContact01().toLowerCase()
                                            : "";

                            String contact02 =
                                    order.getCustomer().getContact02() != null
                                            ? order.getCustomer().getContact02().toLowerCase()
                                            : "";

                            boolean matches =
                                    customerName.contains(searchLower)
                                            || waybill.contains(searchLower)
                                            || contact01.contains(searchLower)
                                            || contact02.contains(searchLower);

                            if (!matches) {
                                return false;
                            }
                        }

                        return true;
                    })
                    .sorted(Comparator.comparing(Order::getDate).reversed())
                    .collect(Collectors.toList());

            long totalElements = filtered.size();

            int totalPages =
                    totalElements == 0
                            ? 0
                            : (int) Math.ceil((double) totalElements / size);

            if (page < 0) {
                page = 0;
            }

            if (totalElements > 0 && page >= totalPages) {
                page = totalPages - 1;
            }

            int startIndex = page * size;

            int endIndex =
                    (int) Math.min((long) startIndex + size, totalElements);

            List<OrderDtoGet> content =
                    filtered.subList(startIndex, endIndex)
                            .stream()
                            .map(order -> {

                                OrderDtoGet dto =
                                        mapperService.map(order, OrderDtoGet.class);

                                dto.setCustomer(
                                        mapperService.map(
                                                order.getCustomer(),
                                                CustomerDto.class));

                                dto.setOrderDetails(getOrderDetailsData(order));

                                return dto;
                            })
                            .collect(Collectors.toList());

            PaginationResponse<OrderDtoGet> response =
                    new PaginationResponse<>();

            response.setContent(content);
            response.setPageNumber(page);
            response.setPageSize(size);
            response.setTotalElements(totalElements);
            response.setTotalPages(totalPages);
            response.setLastPage(
                    totalElements == 0 || page == totalPages - 1);

            return response;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error fetching paginated orders",
                    e);
        }
    }

    @Async("orderExecutor")
    @Override
    public void updateOrderDetails(UserDto userDto) {

        List<Order> recentOrders;

        if (isAdmin(userDto.getRole())) {
            recentOrders = orderRepo.findAllByOrderByOrderIdDesc();
        } else {
            recentOrders = orderRepo.findByUserIdOrderByOrderIdDesc(userDto.getId());
        }

        // ✅ Filter valid orders
        List<Order> filteredOrders = recentOrders.stream()
                .filter(order -> isValidOrder(order))
                .collect(Collectors.toList());

        // ✅ Limit to 5 parallel threads
        ExecutorService limitedExecutor = Executors.newFixedThreadPool(5);

        try {
            List<CompletableFuture<Void>> futures = filteredOrders.stream()
                    .map(order -> CompletableFuture.runAsync(() -> processOrder(order), limitedExecutor))
                    .collect(Collectors.toList());

            // ✅ Wait with timeout
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .orTimeout(5, TimeUnit.MINUTES)
                    .exceptionally(ex -> {
                        System.err.println("Error updating tracking status: " + ex.getMessage());
                        return null;
                    })
                    .join();

        } finally {
            limitedExecutor.shutdown(); // 🔥 important
        }
    }

    // 🔹 Process single order
    private void processOrder(Order order) {
        try {
            String value = checkTrackingStatus(order.getTrackingId());

            if (value != null && !value.equals(order.getStatus())) {
                order.setStatus(value);
                order.setDeliveryDate(LocalDateTime.now());
                orderRepo.save(order);
            }

        } catch (Exception e) {
            System.err.println("Error processing order " + order.getTrackingId() + ": " + e.getMessage());
        }
    }

    // 🔹 Validation
    private boolean isValidOrder(Order order) {
        return !(order.getStatus().equals("Delivered")
                || order.getStatus().equals("Received by Client")
                || order.getStatus().equals("NotFound"))
                && !order.getTrackingId().equals("TRK");
    }

    // 🔹 Role check
    private boolean isAdmin(String role) {
        return role.equalsIgnoreCase("ADMIN")
                || role.equalsIgnoreCase("SUPER USER");
    }

    // 🔥 API CALL (with timeout safe handling)
    private String checkTrackingStatus(String id) {

        String apiUrl = "https://api.transexpress.lk/api/v1/tracking?waybill_id=" + id;

        try {
            String response = restTemplate.getForObject(apiUrl, String.class);

            JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
            JsonArray dataArray = jsonResponse.getAsJsonArray("data");

            for (int i = 0; i < dataArray.size(); i++) {
                JsonObject dataItem = dataArray.get(i).getAsJsonObject();

                if ("tracking_history".equals(dataItem.get("key").getAsString())) {

                    JsonArray historyArray = dataItem.getAsJsonArray("value");

                    if (historyArray.size() > 0) {
                        JsonObject lastStatusItem =
                                historyArray.get(historyArray.size() - 1).getAsJsonObject();

                        return lastStatusItem.get("status_name").getAsString();
                    }
                }
            }

            return null;

        } catch (Exception e) {
            System.err.println("API error for tracking ID " + id + ": " + e.getMessage());
            return null;
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

        if (Objects.equals(userDto.getRole(), "admin") || Objects.equals(userDto.getRole(), "ADMIN")
                || Objects.equals(userDto.getRole(), "SUPER USER") || Objects.equals(userDto.getRole(), "SUPERUSER")) {
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
            Order order = orderRepo.findById(orderId).orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

            // Update Order
            if ("TEMPORARY".equals(order.getStatus())) {
                order.setStatus("PENDING");
                order.getCustomer().setStatus("PENDING");
            } else {
                order.setStatus(requestDTO.getStatus());
                order.setDeliveryDate(LocalDateTime.now());
            }

            order.setTotalPrice(requestDTO.getTotalPrice());
            order.setRemark(requestDTO.getRemark());
            order.setDeliveryDate(requestDTO.getDeliveryDate());

            Order savedOrder = orderRepo.save(order);

            // Delete existing Order Details
           // orderDetailsRepo.deleteByOrder(savedOrder);
            // OR
            orderDetailsRepo.deleteAllByOrder(savedOrder);

            // Save new Order Details
            List<OrderDetails> orderDetailsList = requestDTO.getItems().stream()
                    .map(item -> {
                        Product product = productRepository.findAllByProductId(item.getProductId());

                        if (product == null) {
                            throw new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Product not found with id: " + item.getProductId());
                        }

                        OrderDetails details = new OrderDetails();
                        details.setOrder(savedOrder);
                        details.setProduct(product);
                        details.setQty(item.getQty());          // if available
                        details.setTotal(item.getTotal());      // if available

                        return details;
                    })
                    .collect(Collectors.toList());

            orderDetailsRepo.saveAll(orderDetailsList);

            OrderDtoGet dto = mapperService.map(savedOrder, OrderDtoGet.class);

            if (savedOrder.getCustomer() != null) {
                dto.setCustomer(mapperService.map(savedOrder.getCustomer(), CustomerDto.class));
            }

            dto.setOrderDetails(getOrderDetailsData(savedOrder));

            return dto;

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error resolving order: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Object deleteOrder(Integer orderId) {
        try {
            Order order = orderRepo.findAllByOrderId(orderId);

            if (order == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
            }
            // Customer customer = order.getCustomer();

            if (order.getStatus().equals("PENDING") || order.getStatus().equals("TEMPORARY")) {
                List<OrderDetails> details = orderDetailsRepo.findByOrder(order);
                if (details != null && !details.isEmpty()) {
                    orderDetailsRepo.deleteAll(details);
                }
                orderRepo.delete(order);
                Optional<Customer> byId = customerRepo.findById(order.getCustomer().getCustomerId());
                if (byId.isPresent()) {
                    Customer customer = byId.get();
                    if (order.getStatus().equals("PENDING")) {
                        customer.setStatus("PENDING");
                    } else {
                        customer.setStatus("PENDING");
                    }
                    customerRepo.save(customer);
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