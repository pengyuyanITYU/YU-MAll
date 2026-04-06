package com.yu.order.service.impl;

import com.yu.api.dto.OrderDetailDTO;
import com.yu.order.domain.po.OrderDetail;
import com.yu.order.domain.vo.OrderDetailVO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class OrderDetailContractsTest {

    @Test
    void orderDetailContracts_shouldExposeSkuIdAcrossOrderFlow() {
        assertDoesNotThrow(() -> OrderDetailDTO.class.getDeclaredField("skuId"));
        assertDoesNotThrow(() -> com.yu.order.domain.dto.OrderDetailDTO.class.getDeclaredField("skuId"));
        assertDoesNotThrow(() -> OrderDetail.class.getDeclaredField("skuId"));
        assertDoesNotThrow(() -> OrderDetailVO.class.getDeclaredField("skuId"));
    }
}
