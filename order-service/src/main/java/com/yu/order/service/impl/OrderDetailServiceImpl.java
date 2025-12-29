package com.yu.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yu.api.client.ItemClient;
import com.yu.api.vo.ItemDetailVO;
import com.yu.common.domain.AjaxResult;
import com.yu.order.domain.dto.CommentDTO;
import com.yu.order.domain.dto.OrderDetailDTO;
import com.yu.order.domain.enums.OrderStatus;
import com.yu.order.domain.po.Order;
import com.yu.order.domain.po.OrderDetail;
import com.yu.order.domain.vo.OrderDetailVO;
import com.yu.order.mapper.OrderDetailMapper;
import com.yu.order.service.IOrderDetailService;
import com.yu.order.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements IOrderDetailService {

    private final ItemClient itemClient;

    @Autowired
    @Lazy
    private  IOrderService orderService;

    @Override
    public List<OrderDetailVO> getByOrderId(Long id) {
        log.info("开始查询订单{}的详情", id);
        List<OrderDetail> list = lambdaQuery().eq(OrderDetail::getOrderId, id).list();
        List<OrderDetailVO> orderDetailVOList = list.stream().map(
                orderDetail -> {
                    OrderDetailVO orderDetailVO = new OrderDetailVO();
                    orderDetailVO.setId(orderDetail.getId());
                    orderDetailVO.setItemId(orderDetail.getItemId());
                    orderDetailVO.setNum(orderDetail.getNum());
                    orderDetailVO.setName(orderDetail.getName());
                    orderDetailVO.setSpec(orderDetail.getSpec());
                    orderDetailVO.setPrice(orderDetail.getPrice());
                    orderDetailVO.setImage(orderDetail.getImage());
                    return orderDetailVO;
                }
        ).collect(Collectors.toList());
        log.info("查询订单详情{}", orderDetailVOList);
        return orderDetailVOList;
    }

    @Override
    public boolean addOrderDetails(List<OrderDetailDTO> orderDetailDTOList, Long orderId) {
        return false;
    }


    @Override
    public void deleteByOrderId(Long id) {
        boolean remove = lambdaUpdate().eq(OrderDetail::getOrderId, id).remove();
        if(!remove){
            log.error("删除订单{}的详情失败", id);
            throw new RuntimeException("删除订单详情失败");
        }
    }

    @Override
    public void deleteByOrderIds(List<Long> ids) {
        boolean remove = lambdaUpdate().in(OrderDetail::getOrderId, ids).remove();
        if(!remove){
            log.error("批量删除订单{}的详情失败", ids);
            throw new RuntimeException("批量删除订单详情失败");
        }
    }




}
