package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = "com.example.backend")
@EntityScan(basePackages = {
    "com.example.backend.authlogin.domain",
    "com.example.backend.admin.domain",
    "com.example.backend.fe_hotel_detail.domain",
    "com.example.backend.hotel_reservation.domain",
    "com.example.backend.payment.domain",
    "com.example.backend.review.domain"
})
public class MyBackendApplication {
  public static void main(String[] args) {
    SpringApplication.run(MyBackendApplication.class, args);
  }
}
