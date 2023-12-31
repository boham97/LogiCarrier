package com.example.orderservice.dto;

import lombok.Builder;
import lombok.Data;

import javax.persistence.Column;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class Payload {
    private Long id;
    private List<ResponseItem> responseItemList;
}