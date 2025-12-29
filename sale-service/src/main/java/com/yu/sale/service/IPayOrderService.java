package com.yu.sale.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yu.sale.domain.dto.PayOrderFormDTO;
import com.yu.sale.domain.po.PayOrder;
import com.yu.sale.domain.vo.SaleDashboardVO;


public interface IPayOrderService extends IService<PayOrder> {

    /**
     * 获取销量看板数据
     * @return 看板VO
     */
    SaleDashboardVO getSaleDashboardData();
}