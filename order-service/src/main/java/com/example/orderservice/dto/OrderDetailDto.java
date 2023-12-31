package com.example.orderservice.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@RequiredArgsConstructor
public class OrderDetailDto {
    private long id;
    private String createdTime;
    private String finishedTime;
    private Integer status;
    private Long userId;
    private List<DetailResponseItem> responseItemList;
}
