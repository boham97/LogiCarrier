package com.example.orderservice.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ResponseOrder {
    private Long id;
    private String createdTime;
    private List<ResponseItem> responseItemList;
}
