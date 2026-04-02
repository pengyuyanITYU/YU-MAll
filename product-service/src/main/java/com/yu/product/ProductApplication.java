package com.yu.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.yu.api.client")
@SpringBootApplication(scanBasePackages = "com.yu")
@MapperScan({
        "com.yu.item.mapper",
        "com.yu.comment.mapper",
        "com.yu.collect.mapper"
})
@EnableCaching
public class ProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
    }
}
