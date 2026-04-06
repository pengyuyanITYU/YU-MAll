package com.yu.order.service.impl;

import com.yu.api.client.ItemClient;
import com.yu.api.vo.ItemDetailVO;
import com.yu.common.domain.AjaxResult;
import com.yu.order.domain.dto.OrderDetailDTO;
import com.yu.order.domain.po.OrderDetail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderDetailServiceImplTest {

    @Mock
    private ItemClient itemClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

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
        verify(rabbitTemplate).convertAndSend(eq("order-item-service.direct"), eq("item-sold"), captor.capture());
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
}
