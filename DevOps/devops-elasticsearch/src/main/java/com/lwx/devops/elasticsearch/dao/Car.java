package com.lwx.devops.elasticsearch.dao;

import lombok.Data;

import java.util.Date;

@Data
public class Car {
    private String color;
    private String make;
    private Long price;
    private String sold_date;
    private Long timestamp;

}
