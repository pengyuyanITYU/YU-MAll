package com.yu.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;

@FeignClient(contextId = "cartClient", value="yu-mall-order-service",path="/carts")
public interface CartClient {

}
