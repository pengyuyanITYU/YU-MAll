package com.yu.order.service.impl;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.yu.api.client.AddressClient;
import com.yu.common.domain.AjaxResult;
import com.yu.common.utils.UserContext;
import com.yu.order.domain.po.Order;
import com.yu.order.domain.vo.OrderVO;
import com.yu.order.service.IOrderDetailService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private AddressClient addressClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private IOrderDetailService orderDetailService;

    @Mock(answer = Answers.RETURNS_SELF)
    private LambdaQueryChainWrapper<Order> lambdaQueryWrapper;

    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        orderService = spy(new OrderServiceImpl(addressClient, rabbitTemplate));
        ReflectionTestUtils.setField(orderService, "orderDetailService", orderDetailService);
    }

    @AfterEach
    void tearDown() {
        UserContext.removeUser();
    }

    @Test
    void listById_shouldUseSnapshotReceiverInfo_whenAddressServiceReturnsNullData() {
        UserContext.setUser(37L);
        Order order = new Order();
        order.setId(1L);
        order.setUserId(37L);
        order.setAddressId(100L);
        order.setReceiverContact("snapshot-contact");
        order.setReceiverMobile("13800000000");
        order.setReceiverAddress("snapshot-address");

        doReturn(lambdaQueryWrapper).when(orderService).lambdaQuery();
        when(lambdaQueryWrapper.eq(org.mockito.ArgumentMatchers.<SFunction<Order, ?>>any(), any())).thenReturn(lambdaQueryWrapper);
        when(lambdaQueryWrapper.list()).thenReturn(Collections.singletonList(order));
        when(addressClient.getAddressById(anyLong())).thenReturn(AjaxResult.error("address service unavailable"));
        when(orderDetailService.getByOrderId(1L)).thenReturn(Collections.emptyList());

        List<OrderVO> result = orderService.listById(37L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("snapshot-contact", result.get(0).getReceiverContact());
        assertEquals("13800000000", result.get(0).getReceiverMobile());
        assertEquals("snapshot-address", result.get(0).getReceiverAddress());
    }
}
