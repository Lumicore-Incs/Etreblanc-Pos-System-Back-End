package com.selling.service.impl;

import com.selling.model.Customer;
import com.selling.model.Order;
import com.selling.model.OrderDetails;
import com.selling.model.Product;
import com.selling.repository.CustomerRepo;
import com.selling.repository.OrderRepo;
import com.selling.repository.ProductRepo;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ExcelImportService {
	private static final String SAMPLE_VALUE = "sample";
	private static final String MISSING_CONTACT01 = "000000000";

	private final ProductRepo productRepo;
	private final CustomerRepo customerRepo;
	private final OrderRepo orderRepo;
	private final JdbcTemplate jdbcTemplate;

	@Transactional
	public Map<String, Object> importOrders(MultipartFile file) {
		Map<String, Product> productsByShortName = new HashMap<>();
		for (Product product : productRepo.findAll()) {
			String key = normalize(product.getShortName());
			if (key != null && productsByShortName.putIfAbsent(key, product) != null) {
				throw new IllegalStateException("Duplicate product shortName in products table: " + product.getShortName());
			}
		}
		List<ImportRow> parsedRows = readRows(file, productsByShortName);
		if (parsedRows.isEmpty()) {
			throw new IllegalArgumentException("Excel file has no order rows");
		}

		Map<String, ImportRow> firstRowBySerial = new LinkedHashMap<>();
		int duplicateRowsSkipped = 0;
		for (ImportRow row : parsedRows) {
			if (firstRowBySerial.putIfAbsent(row.serialNo(), row) != null) {
				duplicateRowsSkipped++;
			}
		}
		List<ImportRow> rows = new ArrayList<>(firstRowBySerial.values());
		Set<String> serialNumbers = new LinkedHashSet<>(firstRowBySerial.keySet());
		List<Order> existingOrders = orderRepo.findBySerialNoIn(new ArrayList<>(serialNumbers));
		Set<String> existingSerialNumbers = new LinkedHashSet<>();
		for (Order existingOrder : existingOrders) {
			existingSerialNumbers.add(existingOrder.getSerialNo());
		}
		int existingRowsSkipped = 0;
		for (ImportRow row : rows) {
			if (existingSerialNumbers.contains(row.serialNo())) {
				existingRowsSkipped++;
			}
		}
		rows.removeIf(row -> existingSerialNumbers.contains(row.serialNo()));
		duplicateRowsSkipped += existingRowsSkipped;
		if (rows.isEmpty()) {
			return Map.of(
					"success", true,
					"ordersImported", 0,
					"orderDetailsImported", 0,
					"duplicateRowsSkipped", duplicateRowsSkipped);
		}

		Set<String> phoneNumbers = new LinkedHashSet<>();
		for (ImportRow row : rows) {
			addPhone(phoneNumbers, row.contact01());
			addPhone(phoneNumbers, row.contact02());
		}
		Map<String, Customer> customersByPhone = new HashMap<>();
		if (!phoneNumbers.isEmpty()) {
			for (Customer customer : customerRepo.findByContact01InOrContact02In(phoneNumbers, phoneNumbers)) {
				addCustomerPhone(customersByPhone, customer.getContact01(), customer);
				addCustomerPhone(customersByPhone, customer.getContact02(), customer);
			}
		}

		List<Customer> newCustomers = new ArrayList<>();
		Map<Integer, Customer> customerByRow = new HashMap<>();
		for (ImportRow row : rows) {
			Customer customer = findCustomer(customersByPhone, row);
			boolean isNewCustomer = customer == null;
			if (customer == null) {
				customer = new Customer();
				customer.setStatus("PENDING");
				newCustomers.add(customer);
			}
			customer.setName(row.customerName() != null ? row.customerName()
					: customer.getName() == null ? SAMPLE_VALUE : customer.getName());
			customer.setCustomerName(row.customerName() != null ? row.customerName()
					: customer.getCustomerName() == null ? SAMPLE_VALUE : customer.getCustomerName());
			customer.setAddress(row.address() != null ? row.address()
					: customer.getAddress() == null ? SAMPLE_VALUE : customer.getAddress());
			customer.setContact01(row.contact01() != null ? row.contact01()
					: customer.getContact01() == null ? MISSING_CONTACT01 : customer.getContact01());
			customer.setContact02(row.contact02() != null ? row.contact02()
					: customer.getContact02() == null ? SAMPLE_VALUE : customer.getContact02());
			customerByRow.put(row.rowNumber(), customer);
			if (isNewCustomer) {
				addCustomerPhone(customersByPhone, row.contact01(), customer);
				addCustomerPhone(customersByPhone, row.contact02(), customer);
			}
		}
		customerRepo.saveAll(newCustomers);

		List<Order> orders = new ArrayList<>();
		Map<String, List<OrderDetails>> detailsBySerial = new LinkedHashMap<>();
		for (ImportRow row : rows) {
			Order order = new Order();
			order.setSerialNo(row.serialNo());
			order.setCustomer(customerByRow.get(row.rowNumber()));
			order.setDate(LocalDateTime.now());
			order.setTrackingId("TRK");
			order.setStatus("PENDING");
			order.setTotalPrice(row.totalPrice());
			orders.add(order);

			List<OrderDetails> orderDetails = new ArrayList<>();
			for (ProductQuantity item : row.products()) {
				if (item.product().getPrice() == null) {
					throw rowError(row.rowNumber(), "product has no price: " + item.product().getShortName());
				}
				OrderDetails detail = new OrderDetails();
				detail.setProduct(item.product());
				detail.setQty(item.qty());
				detail.setTotal(item.product().getPrice().multiply(BigDecimal.valueOf(item.qty())));
				orderDetails.add(detail);
			}
			detailsBySerial.put(row.serialNo(), orderDetails);
		}

		List<OrderDetails> details = new ArrayList<>();
		List<Order> savedOrders = orderRepo.saveAll(orders);
		Map<String, Order> savedOrdersBySerial = new HashMap<>();
		for (Order order : savedOrders) {
			savedOrdersBySerial.put(order.getSerialNo(), order);
		}
		for (Map.Entry<String, List<OrderDetails>> entry : detailsBySerial.entrySet()) {
			Order savedOrder = savedOrdersBySerial.get(entry.getKey());
			for (OrderDetails detail : entry.getValue()) {
				detail.setOrder(savedOrder);
				details.add(detail);
			}
		}
		jdbcTemplate.batchUpdate(
			"INSERT INTO order_details (qty, total, product_id, order_id) VALUES (?, ?, ?, ?)",
			details,
			500,
			(statement, detail) -> {
			    statement.setInt(1, detail.getQty());
			    statement.setBigDecimal(2, detail.getTotal());
			    statement.setInt(3, detail.getProduct().getProductId());
			    statement.setInt(4, detail.getOrder().getOrderId());
			});

		return Map.of(
				"success", true,
				"ordersImported", orders.size(),
				"orderDetailsImported", details.size(),
				"duplicateRowsSkipped", duplicateRowsSkipped);
	}

	private List<ImportRow> readRows(MultipartFile file, Map<String, Product> productsByShortName) {
		try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
			if (workbook.getNumberOfSheets() == 0 || workbook.getSheetAt(0).getPhysicalNumberOfRows() < 2) {
				throw new IllegalArgumentException("Excel file must contain a header row and at least one data row");
			}

			var sheet = workbook.getSheetAt(0);
			DataFormatter formatter = new DataFormatter(Locale.ROOT);
			Map<String, Integer> columns = new HashMap<>();
			Row header = sheet.getRow(sheet.getFirstRowNum());
			for (Cell cell : header) {
				String key = normalize(formatter.formatCellValue(cell));
				if (key != null) {
					columns.putIfAbsent(key, cell.getColumnIndex());
				}
			}

			int serialColumn = requiredColumn(columns, "Order Number", "serialNo", "serial", "orderSerialNo");
			int customerNameColumn = requiredColumn(columns, "Customer Name", "name", "customerName");
			int addressColumn = requiredColumn(columns, "Address", "address");
			int contact01Column = optionalColumn(columns, "Customer Phone No 01", "contact01", "phone01");
			int contact02Column = optionalColumn(columns, "Customer Phone No 02", "contact02", "phone02");
			int totalPriceColumn = optionalColumn(columns, "COD Amount", "totalPrice", "orderTotal");

			Map<Integer, Product> productColumns = new LinkedHashMap<>();
			for (Map.Entry<String, Integer> column : columns.entrySet()) {
				Product product = productsByShortName.get(column.getKey());
				if (product != null) {
					productColumns.put(column.getValue(), product);
				}
			}
			if (productColumns.isEmpty()) {
				throw new IllegalArgumentException("No Excel product columns match product shortName values in the products table");
			}

			List<ImportRow> rows = new ArrayList<>();
			for (int index = sheet.getFirstRowNum() + 1; index <= sheet.getLastRowNum(); index++) {
				Row row = sheet.getRow(index);
				if (row == null || isBlank(row, formatter)) {
					continue;
				}
				int rowNumber = index + 1;
				String serialNo = optionalText(row, serialColumn, formatter);
				if (serialNo == null || !serialNo.toUpperCase(Locale.ROOT).startsWith("S")) {
					continue;
				}
				String customerName = optionalText(row, customerNameColumn, formatter);
				String address = optionalText(row, addressColumn, formatter);
				String contact01 = optionalText(row, contact01Column, formatter);
				String contact02 = optionalText(row, contact02Column, formatter);
				BigDecimal totalPrice = optionalDecimal(row, totalPriceColumn, formatter, rowNumber, "COD Amount");
				List<ProductQuantity> products = new ArrayList<>();
				for (Map.Entry<Integer, Product> productColumn : productColumns.entrySet()) {
					Integer qty = optionalInteger(row, productColumn.getKey(), formatter, rowNumber,
							productColumn.getValue().getShortName());
					if (qty == null || qty == 0) {
						continue;
					}
					if (qty < 0) {
						throw rowError(rowNumber, "quantity cannot be negative for " + productColumn.getValue().getShortName());
					}
					products.add(new ProductQuantity(productColumn.getValue(), qty));
				}
				if (products.isEmpty()) {
					throw rowError(rowNumber, "at least one product quantity (FC, BL, etc.) must be greater than zero");
				}

				rows.add(new ImportRow(
						rowNumber,
						serialNo,
						customerName,
						address,
						contact01,
						contact02,
						totalPrice,
						products));
			}
			return rows;
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not read Excel file", e);
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid Excel file: " + e.getMessage(), e);
		}
	}

	private int requiredColumn(Map<String, Integer> columns, String... aliases) {
		int column = optionalColumn(columns, aliases);
		if (column < 0) {
			throw new IllegalArgumentException("Missing required Excel column: " + aliases[0]
					+ ". Found headers: " + String.join(", ", columns.keySet()));
		}
		return column;
	}

	private int optionalColumn(Map<String, Integer> columns, String... aliases) {
		for (String alias : aliases) {
			Integer column = columns.get(normalize(alias));
			if (column != null) {
				return column;
			}
		}
		return -1;
	}

	private String requiredText(Row row, int column, DataFormatter formatter, int rowNumber, String field) {
		String value = optionalText(row, column, formatter);
		if (value == null) {
			throw rowError(rowNumber, field + " is required");
		}
		return value;
	}

	private String optionalText(Row row, int column, DataFormatter formatter) {
		if (column < 0 || row.getCell(column) == null) {
			return null;
		}
		String value = formatter.formatCellValue(row.getCell(column)).trim();
		return value.isEmpty() ? null : value;
	}

	private Integer optionalInteger(Row row, int column, DataFormatter formatter, int rowNumber, String field) {
		String value = optionalText(row, column, formatter);
		if (value == null) {
			return null;
		}
		try {
			return new BigDecimal(value.replace(",", "")).intValueExact();
		} catch (ArithmeticException | NumberFormatException e) {
			throw rowError(rowNumber, field + " quantity must be a whole number");
		}
	}

	private BigDecimal optionalDecimal(Row row, int column, DataFormatter formatter, int rowNumber, String field) {
		String value = optionalText(row, column, formatter);
		if (value == null) {
			return new BigDecimal("0.00");
		}
		try {
			return new BigDecimal(value.replace(",", ""));
		} catch (NumberFormatException e) {
			throw rowError(rowNumber, field + " must be a valid amount");
		}
	}

	private Customer findCustomer(Map<String, Customer> customersByPhone, ImportRow row) {
		Customer customer = row.contact01() == null ? null : customersByPhone.get(normalizePhone(row.contact01()));
		if (customer == null && row.contact02() != null) {
			customer = customersByPhone.get(normalizePhone(row.contact02()));
		}
		return customer;
	}

	private void addPhone(Set<String> phoneNumbers, String phone) {
		if (isRealContact(phone)) {
			phoneNumbers.add(phone.trim());
		}
	}

	private void addCustomerPhone(Map<String, Customer> customersByPhone, String phone, Customer customer) {
		if (isRealContact(phone)) {
			customersByPhone.putIfAbsent(normalizePhone(phone), customer);
		}
	}

	private boolean isRealContact(String phone) {
		return phone != null && !phone.isBlank()
				&& !normalizePhone(phone).equals(normalizePhone(MISSING_CONTACT01))
				&& !normalizePhone(phone).equals(SAMPLE_VALUE);
	}

	private String normalizePhone(String phone) {
		return phone.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
	}

	private boolean isBlank(Row row, DataFormatter formatter) {
		for (Cell cell : row) {
			if (!formatter.formatCellValue(cell).isBlank()) {
				return false;
			}
		}
		return true;
	}

	private String normalize(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.replaceAll("[^a-zA-Z0-9]", "").toLowerCase(Locale.ROOT);
	}

	private IllegalArgumentException rowError(int rowNumber, String message) {
		return new IllegalArgumentException("Excel row " + rowNumber + ": " + message);
	}

	private record ImportRow(
			int rowNumber,
			String serialNo,
			String customerName,
			String address,
			String contact01,
			String contact02,
			BigDecimal totalPrice,
			List<ProductQuantity> products) {
	}

	private record ProductQuantity(Product product, int qty) {
	}

}