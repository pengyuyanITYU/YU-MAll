package com.yu.usercenter;

import org.mybatis.spring.annotation.MapperScan;
import org.dromara.x.file.storage.spring.EnableFileStorage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.yu")
@MapperScan({
        "com.yu.user.mapper",
        "com.yu.address.mapper",
        "com.yu.member.mapper"
})
@EnableFileStorage
@EnableFeignClients(basePackages = "com.yu.api.client")
public class UserCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserCenterApplication.class, args);
    }
}
