package com.yu.order.service.impl;

import com.yu.api.client.ItemClient;
import com.yu.api.vo.ItemDetailVO;
import com.yu.order.domain.dto.CommentDTO;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.yu.common.domain.AjaxResult;
import com.yu.order.domain.dto.OrderDetailDTO;
import com.yu.order.domain.po.OrderDetail;
import com.yu.order.domain.vo.OrderDetailVO;
import com.yu.order.service.IOrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderDetailServiceImplTest {

    @Mock
    private ItemClient itemClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    void getByOrderId_shouldMapCommentedFlagFromEntity() {
        OrderDetailServiceImpl service = spy(new OrderDetailServiceImpl(itemClient, rabbitTemplate));
        LambdaQueryChainWrapper<OrderDetail> queryWrapper = mock(LambdaQueryChainWrapper.class);
        OrderDetail detail = new OrderDetail();
        detail.setId(11L);
        detail.setOrderId(99L);
        detail.setItemId(2L);
        detail.setSkuId(2002L);
        detail.setNum(1);
        detail.setName("item");
        detail.setPrice(499900L);
        detail.setImage("test.png");
        detail.setSpec(Map.of("颜色", "黑色"));
        detail.setCommented(true);

        doReturn(queryWrapper).when(service).lambdaQuery();
        when(queryWrapper.eq(any(), anyLong())).thenReturn(queryWrapper);
        when(queryWrapper.list()).thenReturn(List.of(detail));

        List<OrderDetailVO> result = service.getByOrderId(99L);

        assertEquals(1, result.size());
        assertTrue(result.get(0).isCommented());
        assertEquals(Map.of("颜色", "黑色"), result.get(0).getSpec());
    }

    @Test
    void addOrderDetails_shouldPersistSkuIdAndSendSerializableSharedDtoList() {
        OrderDetailServiceImpl service = spy(new OrderDetailServiceImpl(itemClient, rabbitTemplate));
        doReturn(true).when(service).saveBatch(anyCollection());

        ItemDetailVO itemDetailVO = new ItemDetailVO();
        itemDetailVO.setId(2L);
        itemDetailVO.setName("item");
        when(itemClient.getItemById(2L)).thenReturn(AjaxResult.success(itemDetailVO));

        OrderDetailDTO detailDTO = new OrderDetailDTO()
                .setItemId(2L)
                .setSkuId(2002L)
                .setNum(1)
                .setPrice(499900L)
                .setImage("test.png")
                .setSpecs(Map.of("颜色", "黑色"));

        service.addOrderDetails(List.of(detailDTO), 99L);

        ArgumentCaptor<List> batchCaptor = ArgumentCaptor.forClass(List.class);
        verify(service).saveBatch(batchCaptor.capture());
        List<?> savedDetails = batchCaptor.getValue();
        assertEquals(1, savedDetails.size());
        assertInstanceOf(OrderDetail.class, savedDetails.get(0));
        OrderDetail savedDetail = (OrderDetail) savedDetails.get(0);
        assertEquals(99L, savedDetail.getOrderId());
        assertEquals(2L, savedDetail.getItemId());
        assertEquals(2002L, savedDetail.getSkuId());
        assertEquals(Map.of("颜色", "黑色"), savedDetail.getSpec());

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(org.mockito.ArgumentMatchers.eq("order-item-service.direct"), org.mockito.ArgumentMatchers.eq("item-sold"), captor.capture());
        Object message = captor.getValue();
        assertInstanceOf(List.class, message);
        List<?> payload = (List<?>) message;
        assertEquals(1, payload.size());
        Object first = payload.get(0);
        assertInstanceOf(com.yu.api.dto.OrderDetailDTO.class, first);
        com.yu.api.dto.OrderDetailDTO sharedDTO = (com.yu.api.dto.OrderDetailDTO) first;
        assertEquals(2L, sharedDTO.getItemId());
        assertEquals(2002L, sharedDTO.getSkuId());
        assertEquals(1, sharedDTO.getNum());
        assertDoesNotThrow(() -> {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
                objectOutputStream.writeObject(payload);
            }
        });
    }

    @Test
    void deleteByOrderId_shouldIgnoreMissingOrderDetails() {
        OrderDetailServiceImpl service = spy(new OrderDetailServiceImpl(itemClient, rabbitTemplate));
        LambdaQueryChainWrapper<OrderDetail> queryWrapper = mock(LambdaQueryChainWrapper.class);
        doReturn(queryWrapper).when(service).lambdaQuery();
        when(queryWrapper.eq(any(), anyLong())).thenReturn(queryWrapper);
        when(queryWrapper.count()).thenReturn(0L);

        assertDoesNotThrow(() -> service.deleteByOrderId(999L));
    }

    @Test
    void deleteByOrderIds_shouldIgnoreWhenNoOrderDetailsMatch() {
        OrderDetailServiceImpl service = spy(new OrderDetailServiceImpl(itemClient, rabbitTemplate));
        LambdaQueryChainWrapper<OrderDetail> queryWrapper = mock(LambdaQueryChainWrapper.class);
        doReturn(queryWrapper).when(service).lambdaQuery();
        when(queryWrapper.in(any(), anyList())).thenReturn(queryWrapper);
        when(queryWrapper.count()).thenReturn(0L);

        assertDoesNotThrow(() -> service.deleteByOrderIds(List.of(999L, 1000L)));
    }

    @Test
    void updateOrderCommented_shouldNotMarkOrderEvaluated_whenOtherDetailsRemainUncommented() {
        OrderDetailServiceImpl service = spy(new OrderDetailServiceImpl(itemClient, rabbitTemplate));
        IOrderService orderService = mock(IOrderService.class);
        ReflectionTestUtils.setField(service, "orderService", orderService);

        LambdaQueryChainWrapper<OrderDetail> targetWrapper = mock(LambdaQueryChainWrapper.class, Answers.RETURNS_SELF);
        LambdaQueryChainWrapper<OrderDetail> listWrapper = mock(LambdaQueryChainWrapper.class, Answers.RETURNS_SELF);

        OrderDetail targetDetail = new OrderDetail();
        targetDetail.setId(11L);
        targetDetail.setOrderId(99L);
        targetDetail.setItemId(2L);
        targetDetail.setCommented(false);

        OrderDetail otherDetail = new OrderDetail();
        otherDetail.setId(12L);
        otherDetail.setOrderId(99L);
        otherDetail.setItemId(3L);
        otherDetail.setCommented(false);

        doReturn(targetWrapper, listWrapper).when(service).lambdaQuery();
        when(targetWrapper.one()).thenReturn(targetDetail);
        when(listWrapper.eq(any(), anyLong())).thenReturn(listWrapper);
        when(listWrapper.list()).thenReturn(List.of(targetDetail, otherDetail));
        doReturn(true).when(service).updateById(any(OrderDetail.class));

        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setOrderId(99L);
        commentDTO.setOrderDetailId(11L);
        commentDTO.setItemId(2L);

        service.updateOrderCommented(commentDTO);

        assertTrue(targetDetail.isCommented());
        verify(orderService, never()).lambdaUpdate();
    }
}
