package com.yu.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yu.api.client.AddressClient;
import com.yu.api.client.ItemClient;
import com.yu.api.po.Address;
import com.yu.api.vo.ItemDetailVO;
import com.yu.common.domain.AjaxResult;
import com.yu.common.utils.UserContext;
import com.yu.order.domain.dto.OrderDetailDTO;
import com.yu.order.domain.dto.OrderFormDTO;
import com.yu.order.domain.dto.UpdateOrderStatusDTO;
import com.yu.order.domain.enums.OrderStatus;
import com.yu.order.domain.enums.PayType;
import com.yu.order.domain.po.Order;
import com.yu.order.domain.vo.OrderDetailVO;
import com.yu.order.domain.vo.OrderVO;
import com.yu.order.mapper.OrderMapper;
import com.yu.order.service.IOrderDetailService;
import com.yu.order.service.IOrderService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    private final AddressClient addressClient;
    private final ItemClient itemClient;

    @Lazy
    @Autowired
    private IOrderDetailService orderDetailService;

    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<OrderVO> listById(Long id) {
        Long userId = UserContext.getUser();
        List<Order> list = lambdaQuery().eq(Order::getUserId, userId).list();
        List<OrderVO> orderVOList = list.stream().map(order -> {
            OrderVO orderVO = new OrderVO();
            AjaxResult<Address> addressResult = addressClient.getAddressById(order.getAddressId());
            Address address = addressResult == null ? null : addressResult.getData();
            List<OrderDetailVO> orderDetailVOList = orderDetailService.getByOrderId(order.getId());
            orderVO.setDetails(orderDetailVOList);
            orderVO.setId(order.getId());
            orderVO.setTotalFee(order.getTotalFee());
            orderVO.setPaymentType(order.getPaymentType());
            orderVO.setStatus(order.getStatus());
            orderVO.setCreateTime(order.getCreateTime());
            orderVO.setPayTime(order.getPayTime());
            orderVO.setConsignTime(order.getConsignTime());
            orderVO.setEndTime(order.getEndTime());

            String receiverContact = order.getReceiverContact();
            String receiverMobile = order.getReceiverMobile();
            String receiverAddress = order.getReceiverAddress();
            if (address != null) {
                if (address.getContact() != null) {
                    receiverContact = address.getContact();
                }
                if (address.getMobile() != null) {
                    receiverMobile = address.getMobile();
                }
                if (address.getStreet() != null) {
                    receiverAddress = address.getStreet();
                }
            }
            orderVO.setReceiverContact(receiverContact);
            orderVO.setReceiverMobile(receiverMobile);
            orderVO.setReceiverAddress(receiverAddress);
            return orderVO;
        }).collect(Collectors.toList());

        log.info("查询用户{}的订单={}", userId, orderVOList);
        return orderVOList;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public boolean addOrder(OrderFormDTO orderFormDTO) {
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new RuntimeException("用户未登录，请检查登录信息");
        }

        AjaxResult<Address> addressData = addressClient.getAddressById(orderFormDTO.getAddressId());
        if (addressData == null || !addressData.isSuccess() || addressData.getData() == null) {
            throw new RuntimeException("收货地址不存在");
        }
        Address address = addressData.getData();
        Long calculatedTotalFee = calculateTotalFee(orderFormDTO);
        if (!Objects.equals(orderFormDTO.getTotalFee(), calculatedTotalFee)) {
            log.warn("订单金额已按店铺运费规则重算, frontendTotalFee={}, backendTotalFee={}", orderFormDTO.getTotalFee(), calculatedTotalFee);
        }

        String detailReceiverAddress = address.getProvince() + address.getCity() + address.getTown() + address.getStreet();
        Order order = new Order();
        order.setConsignTime(LocalDateTime.now().plusDays(1))
                .setUserId(userId)
                .setPaymentType(orderFormDTO.getPaymentType())
                .setCloseTime(LocalDateTime.now().plusMinutes(30L))
                .setStatus(OrderStatus.UNPAID.getValue())
                .setAddressId(orderFormDTO.getAddressId())
                .setReceiverContact(address.getContact())
                .setReceiverMobile(address.getMobile())
                .setReceiverAddress(detailReceiverAddress)
                .setTotalFee(calculatedTotalFee);

        boolean save = save(order);
        if (!save || order.getId() == null) {
            log.error("订单保存失败");
            throw new RuntimeException("订单保存失败");
        }

        boolean result = orderDetailService.addOrderDetails(orderFormDTO.getDetails(), order.getId());
        if (!result) {
            log.error("保存订单明细失败, orderId={}", order.getId());
            throw new RuntimeException("保存订单明细失败");
        }

        String message = order.getId().toString();
        rabbitTemplate.convertAndSend("order-service.direct", "order-cancel", message, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setDelayLong(60000L);
                message.getMessageProperties().setExpiration("10000");
                return message;
            }
        });
        rabbitTemplate.convertAndSend("order-service.direct", "order-confirm", message, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setDelayLong(60000L);
                message.getMessageProperties().setExpiration("10000");
                return message;
            }
        });

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByOrderId(Long id) {
        removeById(id);
        orderDetailService.deleteByOrderId(id);
    }

    @Override
    public void deleteByOrderIds(List<Long> ids) {
        removeByIds(ids);
        orderDetailService.deleteByOrderIds(ids);
    }

    @Override
    public OrderVO getByOrderId(Long id) {
        Long userId = UserContext.getUser();
        Order order = lambdaQuery().eq(Order::getId, id).eq(Order::getUserId, userId).one();
        if (order == null) {
            return null;
        }

        AjaxResult<Address> address = addressClient.getAddressById(order.getAddressId());
        List<OrderDetailVO> orderDetailVOList = orderDetailService.getByOrderId(order.getId());
        OrderVO orderVO = new OrderVO();
        orderVO.setDetails(orderDetailVOList);
        orderVO.setTotalFee(order.getTotalFee());
        orderVO.setPaymentType(order.getPaymentType());
        orderVO.setStatus(order.getStatus());
        orderVO.setPayTime(order.getPayTime());
        orderVO.setConsignTime(order.getConsignTime());
        orderVO.setEndTime(order.getEndTime());
        orderVO.setCreateTime(order.getCreateTime());
        orderVO.setCloseTime(order.getCloseTime());
        orderVO.setId(order.getId());
        if (address != null && address.isSuccess() && address.getData() != null) {
            orderVO.setReceiverContact(address.getData().getContact());
            orderVO.setReceiverMobile(address.getData().getMobile());
            orderVO.setReceiverAddress(address.getData().getStreet());
        } else {
            orderVO.setReceiverContact(order.getReceiverContact());
            orderVO.setReceiverMobile(order.getReceiverMobile());
            orderVO.setReceiverAddress(order.getReceiverAddress());
        }
        log.info("查询用户{}的订单={}", userId, orderVO);
        return orderVO;
    }

    @Override
    public boolean cancelOrder(Long id) {
        return lambdaUpdate().eq(Order::getId, id)
                .set(Order::getStatus, OrderStatus.CANCELED.getValue())
                .set(Order::getCloseTime, LocalDateTime.now())
                .set(Order::getUpdateTime, LocalDateTime.now())
                .update();
    }

    @Override
    public void batchConsignOrders() {
        boolean update = lambdaUpdate()
                .eq(Order::getStatus, OrderStatus.PAID)
                .le(Order::getCreateTime, LocalDateTime.now())
                .set(Order::getStatus, OrderStatus.SHIPPED)
                .set(Order::getConsignTime, LocalDateTime.now())
                .update();
        if (!update) {
            log.info("订单暂不满足发货条件");
        }
    }

    @Override
    public void batchConfirmOrders() {
        boolean update = lambdaUpdate()
                .eq(Order::getStatus, OrderStatus.SHIPPED)
                .le(Order::getCreateTime, LocalDateTime.now())
                .set(Order::getStatus, OrderStatus.SUCCESS)
                .set(Order::getConsignTime, LocalDateTime.now())
                .update();
        if (!update) {
            log.info("订单暂不满足确认条件");
        }
    }

    @Override
    public boolean confirmOrder(Long id) {
        return lambdaUpdate().eq(Order::getId, id)
                .set(Order::getStatus, OrderStatus.SUCCESS.getValue())
                .set(Order::getUpdateTime, LocalDateTime.now())
                .set(Order::getEndTime, LocalDateTime.now())
                .set(Order::getCloseTime, LocalDateTime.now())
                .update();
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public boolean updateOrderStatus(UpdateOrderStatusDTO updateOrderStatusDTO) {
        OrderStatus status = updateOrderStatusDTO.getStatus();
        Long id = updateOrderStatusDTO.getId();
        PayType paymentType = updateOrderStatusDTO.getPaymentType();
        if (id == null) {
            log.error("订单id不能为空");
            throw new RuntimeException("订单id不能为空");
        }
        if (paymentType == null) {
            log.error("订单支付方式不能为空");
            throw new RuntimeException("订单支付方式不能为空");
        }
        if (status == null) {
            log.error("订单状态不能为空");
            throw new RuntimeException("订单状态不能为空");
        }
        return lambdaUpdate()
                .eq(Order::getId, id)
                .set(Order::getStatus, status.getValue())
                .set(Order::getPaymentType, paymentType.getValue())
                .set(Order::getPayTime, LocalDateTime.now())
                .set(Order::getUpdateTime, LocalDateTime.now())
                .update();
    }

    @Override
    public void updateAllPayOrderStatus() {
        lambdaUpdate()
                .eq(Order::getStatus, OrderStatus.UNPAID.getValue())
                .set(Order::getStatus, OrderStatus.PAID.getValue())
                .set(Order::getPayTime, LocalDateTime.now())
                .set(Order::getUpdateTime, LocalDateTime.now())
                .update();
    }

    private Long calculateTotalFee(OrderFormDTO orderFormDTO) {
        if (orderFormDTO == null || orderFormDTO.getDetails() == null || orderFormDTO.getDetails().isEmpty()) {
            throw new RuntimeException("订单明细不能为空");
        }
        long itemTotalFee = 0L;
        Map<Long, ShopSettlement> shopSettlementMap = new HashMap<>();
        for (OrderDetailDTO detail : orderFormDTO.getDetails()) {
            AjaxResult<ItemDetailVO> itemResult = itemClient.getItemById(detail.getItemId());
            if (itemResult == null || !itemResult.isSuccess() || itemResult.getData() == null) {
                throw new RuntimeException("查询商品失败");
            }
            ItemDetailVO item = itemResult.getData();
            if (item.getShopId() == null) {
                throw new RuntimeException("商品未配置店铺");
            }
            long lineTotal = nullToZero(detail.getPrice()) * (detail.getNum() == null ? 0 : detail.getNum());
            itemTotalFee += lineTotal;

            ShopSettlement settlement = shopSettlementMap.computeIfAbsent(item.getShopId(), key ->
                    new ShopSettlement(item.getShippingType(), item.getShippingFee(), item.getFreeShippingThreshold()));
            settlement.addSubtotal(lineTotal);
        }
        long shippingTotalFee = shopSettlementMap.values().stream()
                .mapToLong(this::calculateShopShippingFee)
                .sum();
        return itemTotalFee + shippingTotalFee;
    }

    private long calculateShopShippingFee(ShopSettlement settlement) {
        if (settlement == null || settlement.getShippingType() == null) {
            return 0L;
        }
        if ("FREE".equals(settlement.getShippingType())) {
            return 0L;
        }
        if ("FIXED".equals(settlement.getShippingType())) {
            return nullToZero(settlement.getShippingFee());
        }
        if ("THRESHOLD_FREE".equals(settlement.getShippingType())) {
            long threshold = nullToZero(settlement.getFreeShippingThreshold());
            return settlement.getSubtotal() >= threshold ? 0L : nullToZero(settlement.getShippingFee());
        }
        throw new RuntimeException("不支持的运费模式");
    }

    private long nullToZero(Integer value) {
        return value == null ? 0L : value.longValue();
    }

    private long nullToZero(Long value) {
        return value == null ? 0L : value;
    }

    private static class ShopSettlement {
        private final String shippingType;
        private final Integer shippingFee;
        private final Integer freeShippingThreshold;
        private long subtotal;

        private ShopSettlement(String shippingType, Integer shippingFee, Integer freeShippingThreshold) {
            this.shippingType = shippingType;
            this.shippingFee = shippingFee;
            this.freeShippingThreshold = freeShippingThreshold;
        }

        private void addSubtotal(long amount) {
            this.subtotal += amount;
        }

        public String getShippingType() {
            return shippingType;
        }

        public Integer getShippingFee() {
            return shippingFee;
        }

        public Integer getFreeShippingThreshold() {
            return freeShippingThreshold;
        }

        public long getSubtotal() {
            return subtotal;
        }
    }
}
