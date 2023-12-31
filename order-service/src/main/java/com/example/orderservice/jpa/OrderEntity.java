package com.example.orderservice.jpa;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jdk.jfr.DataAmount;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

import static java.time.LocalDateTime.now;

@Data
@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Integer status;
    @Column(nullable = false)
    private String createdTime;
    @Column(nullable = true)
    private String finishedTime;
    private Long userId;
    @JsonBackReference
    @OneToMany(mappedBy = "orderEntity", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ItemEntity> itemEntityList;
    @Builder
    public OrderEntity(){
        this.status = 0;
        this.createdTime = LocalDateTime.now().toString();
    }
}
