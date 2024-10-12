package com.ntros;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.ntros")
public class TradeMatrixApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradeMatrixApplication.class, args);
    }

}
