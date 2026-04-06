package com.yu.item.service.impl;

import com.yu.common.exception.BusinessException;
import com.yu.item.domain.dto.ShopDTO;
import com.yu.item.mapper.ShopMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ShopServiceImplTest {

    @Mock
    private ShopMapper shopMapper;

    @InjectMocks
    private ShopServiceImpl shopService;

    @Test
    void addShopShouldRejectThresholdRuleWithoutThreshold() {
        ShopDTO dto = new ShopDTO();
        dto.setName("默认旗舰店");
        dto.setIsSelf(1);
        dto.setShippingType("THRESHOLD_FREE");
        dto.setShippingFee(1000);
        dto.setStatus(1);

        assertThrows(BusinessException.class, () -> shopService.addShop(dto));
    }
}
