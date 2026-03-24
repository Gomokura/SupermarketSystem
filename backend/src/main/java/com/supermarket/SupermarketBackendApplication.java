package com.supermarket;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.supermarket.mapper")
public class SupermarketBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SupermarketBackendApplication.class, args);
    }
}
