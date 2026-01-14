package com.selling.util;

import com.selling.dto.get.ExcelTypeDto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelExportService {

    public ByteArrayInputStream exportToExcel(List<ExcelTypeDto> entities) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Data");

            // Header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Name", "Address", "whatsapp No ", "Contact02", "Price"}; // ඔබේ entity අනුව header වෙනස් කරන්න
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Data rows
            int rowNum = 1;
            for (ExcelTypeDto entity : entities) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(entity.getId());
                row.createCell(1).setCellValue(entity.getName());
                row.createCell(2).setCellValue(entity.getAddress());
                row.createCell(3).setCellValue(entity.getContact01());
                row.createCell(4).setCellValue(entity.getContact02());
                row.createCell(5).setCellValue(entity.getPrice());
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Excel don't create: " + e.getMessage());
        }
    }
}