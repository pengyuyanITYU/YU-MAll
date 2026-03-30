package com.yu.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yu.api.client.AddressClient;
import com.yu.api.client.AdminUserClient;
import com.yu.api.po.Address;
import com.yu.api.vo.UserVO;
import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.common.utils.CollUtils;
import com.yu.order.domain.enums.OrderStatus;
import com.yu.order.domain.po.Order;
import com.yu.order.domain.query.OrderPageQuery;
import com.yu.order.domain.vo.OrderDashBoardVO;
import com.yu.order.domain.vo.OrderDetailVO;
import com.yu.order.domain.vo.OrderVO;
import com.yu.order.mapper.OrderMapper;
import com.yu.order.service.IAdminOrderService;
import com.yu.order.service.IOrderDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminOrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IAdminOrderService {

    private final AddressClient addressClient;

    private final AdminUserClient adminUserClient;

    @Lazy
    @Autowired
    private IOrderDetailService orderDetailService;

    @Override
    public TableDataInfo listPage(OrderPageQuery query) {
        Page<Order> page = query.toMpPageDefaultSortByCreateTimeDesc();
        Page<Order> result = lambdaQuery()
                .eq(query.getStatus() != null, Order::getStatus, query.getStatus())
                .eq(query.getUserId() != null, Order::getUserId, query.getUserId())
                .eq(query.getPaymentType() != null, Order::getPaymentType, query.getPaymentType())
                .page(page);
        List<OrderVO> records = BeanUtil.copyToList(result.getRecords(), OrderVO.class);
        if (CollUtils.isNotEmpty(records)) {
            List<Long> userIds = records.stream().map(OrderVO::getUserId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
            if (CollUtils.isNotEmpty(userIds)) {
                AjaxResult<List<UserVO>> users = adminUserClient.getUserInfoByIds(userIds);
                if (users != null && users.isSuccess() && CollUtil.isNotEmpty(users.getData())) {
                    Map<Long, UserVO> userMap = users.getData().stream()
                            .filter(u -> u != null && u.getId() != null)
                            .collect(Collectors.toMap(UserVO::getId, Function.identity(), (a, b) -> a));
                    records.forEach(o -> {
                        UserVO userVO = userMap.get(o.getUserId());
                        if (userVO != null) {
                            o.setNickName(userVO.getNickName());
                        }
                    });
                }
            }
        }
        return TableDataInfo.success(records, result.getTotal());
    }

    @Override
    public OrderVO getByOrderId(Long id) {
        Order order = lambdaQuery().eq(Order::getId, id).one();
        if (order == null) {
            return null;
        }
        OrderVO orderVO = BeanUtil.copyProperties(order, OrderVO.class);
        List<OrderDetailVO> details = orderDetailService.getByOrderId(order.getId());
        orderVO.setDetails(details);
        AjaxResult<Address> address = addressClient.getAddressById(order.getAddressId());
        if (address != null && address.isSuccess() && address.getData() != null) {
            orderVO.setReceiverContact(address.getData().getContact());
            orderVO.setReceiverMobile(address.getData().getMobile());
            orderVO.setReceiverAddress(address.getData().getStreet());
        }
        AjaxResult<List<UserVO>> users = adminUserClient.getUserInfoByIds(Collections.singletonList(order.getUserId()));
        if (users != null && users.isSuccess() && CollUtil.isNotEmpty(users.getData())) {
            orderVO.setNickName(users.getData().get(0).getNickName());
        }
        return orderVO;
    }

    @Override
    public List<OrderDashBoardVO> recentOrders() {
        List<Order> orders = lambdaQuery().orderByDesc(Order::getCreateTime).last("LIMIT 5").list();
        if (CollUtil.isEmpty(orders)) {
            return Collections.emptyList();
        }
        List<Long> userIds = orders.stream().map(Order::getUserId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, UserVO> userMap = Collections.emptyMap();
        if (CollUtil.isNotEmpty(userIds)) {
            AjaxResult<List<UserVO>> users = adminUserClient.getUserInfoByIds(userIds);
            if (users != null && users.isSuccess() && CollUtil.isNotEmpty(users.getData())) {
                userMap = users.getData().stream()
                        .filter(u -> u != null && u.getId() != null)
                        .collect(Collectors.toMap(UserVO::getId, Function.identity(), (a, b) -> a));
            }
        }
        Map<Long, UserVO> finalUserMap = userMap;
        return orders.stream().map(order -> {
            OrderDashBoardVO vo = new OrderDashBoardVO();
            vo.setId(order.getId());
            vo.setCreateTime(order.getCreateTime());
            vo.setTotalPrice(order.getTotalFee());
            vo.setStatus(convertOrderStatus(order.getStatus()));
            UserVO user = finalUserMap.get(order.getUserId());
            if (user != null) {
                vo.setNickName(user.getNickName());
                vo.setAvatar(user.getAvatar());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    private OrderStatus convertOrderStatus(Integer statusValue) {
        if (statusValue == null) {
            return null;
        }
        for (OrderStatus status : OrderStatus.values()) {
            if (status.getValue().equals(statusValue)) {
                return status;
            }
        }
        return null;
    }
}
