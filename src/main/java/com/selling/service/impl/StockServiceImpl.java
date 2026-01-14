package com.selling.service.impl;

import com.selling.dto.StockDto;
import com.selling.model.Order;
import com.selling.model.OrderDetails;
import com.selling.model.Stock;
import com.selling.repository.StockRepo;
import com.selling.service.StockService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockRepo stockRepo;
    private final ModelMapper modelMapper;

    @Override
    public StockDto saveStock(StockDto stockDto) {
        Stock stock = dtoToEntity(stockDto);
        stockRepo.save(stock);
        return entityToDto(stock);
    }

    @Override
    public StockDto getStockById(Long aLong) {
        Integer id = Math.toIntExact(aLong);
        return entityToDto(stockRepo.findById(id).get());
    }

    @Override
    public List<StockDto> getAllStock() {
        List<Stock> all = stockRepo.findAllByOrderByDateDesc();
        List<StockDto> stockDtos = new ArrayList<>();
        for (Stock stock : all) {
            entityToDto(stock);
            stockDtos.add(entityToDto(stock));
        }
        return stockDtos;
    }

    @Override
    public StockDto updateStock(Integer id, StockDto stockDto) {
        Optional<Stock> byId = stockRepo.findById(id);
        Stock stock1 = dtoToEntity(stockDto);
        if (byId.isPresent()) {
            Stock stock = byId.get();
            if (stock.getQuantity()==stock.getTotalQuantity()) {
                stock1.setStock_id(stock.getStock_id());
                stockRepo.save(stock1);
                return entityToDto(stock1);
            }
            return null;
        }
        return null;
    }

    @Override
    public boolean deleteStock(Integer id) {
        Optional<Stock> byId = stockRepo.findById(id);
        if (byId.isPresent()) {
            if (byId.get().getQuantity()==byId.get().getTotalQuantity()) {
                stockRepo.deleteById(id);
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public List<StockDto> getAllStockByType(String name) {
        List<Stock> all = stockRepo.findAllByType(name);
        List<StockDto> stockDtos = new ArrayList<>();
        for (Stock stock : all) {
            entityToDto(stock);
            stockDtos.add(entityToDto(stock));
        }
        return stockDtos;
    }

    @Override
    public void updateStockByName(String name, Integer qty) {
        List<Stock> allByType = stockRepo.findAllByType(name);
        int remainingQty = qty; // මෙකෙන් අපිට අඩු කරන්න තියෙන මුළු qty එක track කරගන්න පුලුවන්

        for (Stock stock : allByType) {
            if (remainingQty <= 0) {
                break; // අඩු කරන්න දෙයක් නැත්තන් loop එක නවත්වන්න
            }

            int currentQty = stock.getQuantity();

            if (currentQty > 0) {
                if (currentQty >= remainingQty) {
                    // මේ stock එකෙන්ම balance එක අඩු කරන්න පුලුවන්
                    stock.setQuantity(currentQty - remainingQty);
                    remainingQty = 0;
                } else {
                    // මේ stock එකෙන් පුරා අඩු කරන්න බැහැ
                    remainingQty -= currentQty;
                    stock.setQuantity(0);
                }

                stockRepo.save(stock);
            }
        }

        if (remainingQty > 0) {
            System.out.println("Warning: Not enough stock to deduct full quantity. Remaining: " + remainingQty);
        } else {
            System.out.println("Stock updated successfully.");
        }
    }


    @Override
    public void updateStockQty(List<OrderDetails> details) {
        for (OrderDetails detail : details) {
            List<Stock> allByType = stockRepo.findAllByType(detail.getProduct().getName());
            Stock stock = allByType.get(allByType.size() - 2);
            stock.setQuantity(stock.getQuantity() + detail.getQty());
            stockRepo.save(stock);
        }
    }

    public Stock dtoToEntity(StockDto stockDto) {
        return modelMapper.map(stockDto, Stock.class);
    }

    public StockDto entityToDto(Stock stock) {
        return modelMapper.map(stock, StockDto.class);
    }
}
