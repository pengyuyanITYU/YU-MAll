package com.yu.order.consumer;


import com.yu.api.client.OrderClient;
import com.yu.order.domain.po.Order;
import com.yu.order.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Component
public class OrderConsumer {

    private final IOrderService orderService;

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "admin-order-consign.queue", durable = "true"), exchange = @Exchange(value = "admin-order-service.topic", durable = "true", type = ExchangeTypes.TOPIC), key = "order-consign"))
    public void orderConsign(String  message){
        log.info("开始进行订单发货");
        List<Order> list = orderService.list();


    }


    }


