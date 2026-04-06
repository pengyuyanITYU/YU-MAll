package com.yu.order.service.impl;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.yu.api.client.AddressClient;
import com.yu.api.client.ItemClient;
import com.yu.api.po.Address;
import com.yu.api.vo.ItemDetailVO;
import com.yu.common.domain.AjaxResult;
import com.yu.common.utils.UserContext;
import com.yu.order.domain.dto.OrderDetailDTO;
import com.yu.order.domain.dto.OrderFormDTO;
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
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private AddressClient addressClient;

    @Mock
    private ItemClient itemClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private IOrderDetailService orderDetailService;

    @Mock(answer = Answers.RETURNS_SELF)
    private LambdaQueryChainWrapper<Order> lambdaQueryWrapper;

    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        orderService = spy(new OrderServiceImpl(addressClient, itemClient, rabbitTemplate));
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

    @Test
    void addOrder_shouldThrow_whenSaveFails() {
        UserContext.setUser(37L);
        when(addressClient.getAddressById(100L)).thenReturn(AjaxResult.success(buildAddress()));
        when(itemClient.getItemById(200L)).thenReturn(AjaxResult.success(buildItemDetail("FIXED", 800, 0, 10L)));
        doReturn(false).when(orderService).save(any(Order.class));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> orderService.addOrder(buildOrderForm()));

        assertEquals("订单保存失败", ex.getMessage());
        verify(orderDetailService, never()).addOrderDetails(anyList(), anyLong());
    }

    @Test
    void addOrder_shouldThrow_whenOrderDetailsSaveFails() {
        UserContext.setUser(37L);
        when(addressClient.getAddressById(100L)).thenReturn(AjaxResult.success(buildAddress()));
        when(itemClient.getItemById(200L)).thenReturn(AjaxResult.success(buildItemDetail("FIXED", 800, 0, 10L)));
        doAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(99L);
            return true;
        }).when(orderService).save(any(Order.class));
        when(orderDetailService.addOrderDetails(anyList(), eq(99L))).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> orderService.addOrder(buildOrderForm()));

        assertEquals("保存订单明细失败", ex.getMessage());
    }

    @Test
    void addOrder_shouldRecalculateTotalFeeByShopShippingRule() {
        UserContext.setUser(37L);
        when(addressClient.getAddressById(100L)).thenReturn(AjaxResult.success(buildAddress()));
        when(itemClient.getItemById(200L)).thenReturn(AjaxResult.success(buildItemDetail("FIXED", 800, 0, 10L)));

        AtomicReference<Order> savedOrderRef = new AtomicReference<>();
        doAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(99L);
            savedOrderRef.set(order);
            return true;
        }).when(orderService).save(any(Order.class));
        when(orderDetailService.addOrderDetails(anyList(), eq(99L))).thenReturn(true);

        orderService.addOrder(buildOrderForm());

        assertNotNull(savedOrderRef.get());
        assertEquals(2799L, savedOrderRef.get().getTotalFee());
    }

    private static Address buildAddress() {
        return new Address()
                .setId(100L)
                .setContact("snapshot-contact")
                .setMobile("13800000000")
                .setProvince("上海市")
                .setCity("上海市")
                .setTown("浦东新区")
                .setStreet("世纪大道100号");
    }

    private static ItemDetailVO buildItemDetail(String shippingType, Integer shippingFee, Integer threshold, Long shopId) {
        ItemDetailVO itemDetailVO = new ItemDetailVO();
        itemDetailVO.setId(200L);
        itemDetailVO.setName("测试商品");
        itemDetailVO.setShopId(shopId);
        itemDetailVO.setShippingType(shippingType);
        itemDetailVO.setShippingFee(shippingFee);
        itemDetailVO.setFreeShippingThreshold(threshold);
        return itemDetailVO;
    }

    private static OrderFormDTO buildOrderForm() {
        OrderDetailDTO detailDTO = new OrderDetailDTO()
                .setItemId(200L)
                .setNum(1)
                .setPrice(1999L)
                .setImage("test.png")
                .setSpecs(Map.of("颜色", "黑色"));
        OrderFormDTO orderFormDTO = new OrderFormDTO();
        orderFormDTO.setAddressId(100L);
        orderFormDTO.setPaymentType(1);
        orderFormDTO.setTotalFee(1999L);
        orderFormDTO.setDetails(Collections.singletonList(detailDTO));
        return orderFormDTO;
    }
}
