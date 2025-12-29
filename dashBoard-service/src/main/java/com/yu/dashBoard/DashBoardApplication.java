package com.yu.dashBoard;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.yu.api.client")
@MapperScan(basePackages = "com.yu.dashBoard.mapper")
public class DashBoardApplication {
    public static void main(String[] args) {
       SpringApplication.run(DashBoardApplication.class, args);
    }
}
