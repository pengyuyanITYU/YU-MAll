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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements IOrderDetailService {

    private final ItemClient itemClient;

    @Autowired
    @Lazy
    private IOrderService orderService;

    private final RabbitTemplate rabbitTemplate;

    @Override
    public List<OrderDetailVO> getByOrderId(Long id) {
        log.info("query order details by orderId={}", id);
        List<OrderDetail> list = lambdaQuery().eq(OrderDetail::getOrderId, id).list();
        List<OrderDetailVO> orderDetailVOList = list.stream().map(orderDetail -> {
            OrderDetailVO orderDetailVO = new OrderDetailVO();
            orderDetailVO.setId(orderDetail.getId());
            orderDetailVO.setItemId(orderDetail.getItemId());
            orderDetailVO.setNum(orderDetail.getNum());
            orderDetailVO.setName(orderDetail.getName());
            orderDetailVO.setSpec(orderDetail.getSpec());
            orderDetailVO.setPrice(orderDetail.getPrice());
            orderDetailVO.setImage(orderDetail.getImage());
            return orderDetailVO;
        }).collect(Collectors.toList());
        log.info("query order details result size={}", orderDetailVOList.size());
        return orderDetailVOList;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean addOrderDetails(@Valid @NotEmpty(message = "订单明细不能为空") List<OrderDetailDTO> orderDetailDTOList, Long orderId) {
        log.info("create order details orderId={}, size={}", orderId, orderDetailDTOList.size());

        List<OrderDetail> orderDetailList = orderDetailDTOList.stream().map(orderDetailDTO -> {
            // 购物车存储的是商品ID(Item ID)，使用 getItemById 查询
            AjaxResult<ItemDetailVO> itemData = itemClient.getItemById(orderDetailDTO.getItemId());
            if (itemData == null || !itemData.isSuccess()) {
                log.error("query item failed, skuId={}, code={}", orderDetailDTO.getItemId(), itemData != null ? itemData.getCode() : null);
                throw new RuntimeException("查询商品失败");
            }
            ItemDetailVO item = itemData.getData();
            if (item == null) {
                log.error("item not found, skuId={}", orderDetailDTO.getItemId());
                throw new RuntimeException("商品不存在");
            }
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId)
                    .setItemId(item.getId())
                    .setNum(orderDetailDTO.getNum())
                    .setName(item.getName())
                    .setSpec(orderDetailDTO.getSpecs())
                    .setImage(orderDetailDTO.getImage())
                    .setPrice(orderDetailDTO.getPrice());
            return orderDetail;
        }).collect(Collectors.toList());

        boolean result = saveBatch(orderDetailList);
        if (!result) {
            log.error("save order details failed, orderId={}", orderId);
            throw new RuntimeException("保存订单明细失败");
        }
        rabbitTemplate.convertAndSend("order-item-service.direct", "item-sold", buildItemSoldMessage(orderDetailDTOList));
        log.info("save order details success, orderId={}", orderId);
        return result;
    }

    private List<com.yu.api.dto.OrderDetailDTO> buildItemSoldMessage(List<OrderDetailDTO> orderDetailDTOList) {
        return orderDetailDTOList.stream()
                .map(detail -> new com.yu.api.dto.OrderDetailDTO()
                        .setItemId(detail.getItemId())
                        .setNum(detail.getNum()))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByOrderId(Long id) {
        boolean remove = lambdaUpdate().eq(OrderDetail::getOrderId, id).remove();
        if (!remove) {
            log.error("delete order details failed, orderId={}", id);
            throw new RuntimeException("删除订单明细失败");
        }
    }

    @Override
    public void deleteByOrderIds(List<Long> ids) {
        boolean remove = lambdaUpdate().in(OrderDetail::getOrderId, ids).remove();
        if (!remove) {
            log.error("batch delete order details failed, orderIds={}", ids);
            throw new RuntimeException("批量删除订单明细失败");
        }
    }

    @Override
    public void updateOrderCommented(CommentDTO commentDTO) {
        OrderDetail one = lambdaQuery()
                .eq(commentDTO.getOrderId() != null, OrderDetail::getOrderId, commentDTO.getOrderId())
                .eq(commentDTO.getOrderDetailId() != null, OrderDetail::getId, commentDTO.getOrderDetailId())
                .eq(commentDTO.getItemId() != null, OrderDetail::getItemId, commentDTO.getItemId())
                .one();
        if (one == null) {
            log.error("order detail not found, orderId={}, detailId={}", commentDTO.getOrderId(), commentDTO.getOrderDetailId());
            throw new RuntimeException("订单明细不存在");
        }

        one.setCommented(true);
        boolean update = updateById(one);
        if (!update) {
            log.error("update order detail commented failed, orderId={}, detailId={}", commentDTO.getOrderId(), commentDTO.getOrderDetailId());
            throw new RuntimeException("更新订单明细失败");
        }

        boolean updateOrder = orderService.lambdaUpdate()
                .eq(Order::getId, commentDTO.getOrderId())
                .set(Order::getStatus, OrderStatus.EVALUATED)
                .set(Order::getCommentTime, LocalDateTime.now())
                .set(Order::getEndTime, LocalDateTime.now())
                .update();
        if (!updateOrder) {
            log.error("update order status evaluated failed, orderId={}", commentDTO.getOrderId());
            throw new RuntimeException("更新订单状态失败");
        }
    }
}
