package com.yu.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yu.api.client.AddressClient;
import com.yu.api.client.UserClient;
import com.yu.api.po.Address;
import com.yu.api.vo.UserVO;
import com.yu.common.constant.HttpStatus;
import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.common.utils.AdministratorContext;
import com.yu.common.utils.CollUtils;
import com.yu.order.domain.dto.OrderFormDTO;
import com.yu.order.domain.enums.OrderStatus;
import com.yu.order.domain.po.Order;
import com.yu.order.domain.query.OrderPageQuery;
import com.yu.order.domain.vo.OrderDashBoardVO;
import com.yu.order.domain.vo.OrderDetailVO;
import com.yu.order.domain.vo.OrderVO;
import com.yu.order.mapper.OrderMapper;
import com.yu.order.service.IOrderDetailService;
import com.yu.order.service.IOrderService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.atn.SemanticContext;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    private final AddressClient addressClient;

    private final UserClient userClient;

    private final RabbitTemplate rabbitTemplate;

    @Lazy
    @Autowired
    private IOrderDetailService orderDetailService;

    @Override
    public TableDataInfo listPage(OrderPageQuery query) {
        Page<Order> pageParams = query.toMpPageDefaultSortByCreateTimeDesc();
        log.info("开始进行列表查询");
        Page<Order> resultPage = lambdaQuery()
                .eq(query.getStatus() != null, Order::getStatus, query.getStatus())
                .eq(query.getUserId() != null, Order::getUserId, query.getUserId())
                .eq(query.getPaymentType() != null, Order::getPaymentType, query.getPaymentType())
                .page(pageParams);
        OrderItem orderItem = new OrderItem();
        if(query.getSortBy() != null){
            orderItem.setColumn(query.getSortBy());
            orderItem.setAsc(query.isAsc());
        }else{
            orderItem.setColumn("createTime");
            orderItem.setAsc(query.isAsc());
        }
        resultPage.addOrder(orderItem);
        List<OrderVO> voList = BeanUtil.copyToList(resultPage.getRecords(), OrderVO.class);
        List<Long> collect = voList.stream().map(OrderVO::getUserId).collect(Collectors.toList());
        if (CollUtils.isNotEmpty(collect)) {
            try {
                AjaxResult<List<UserVO>> userResult = userClient.getUserInfoByIds(collect);
                if (userResult != null && CollUtil.isNotEmpty(userResult.getData())) {
                    // 将用户列表转换为Map，提高查询效率
                    Map<Long, UserVO> userMap = userResult.getData().stream()
                            .filter(userVO -> userVO != null && userVO.getId() != null)
                            .collect(Collectors.toMap(UserVO::getId, Function.identity()));

                    // 根据userId匹配设置昵称
                    voList.forEach(orderVO -> {
                        UserVO userVO = userMap.get(orderVO.getUserId());
                        if (userVO != null) {
                            orderVO.setNickName(userVO.getNickName());
                        }
                    });
                } else {
                    log.warn("userClient获取到的用户信息为空或结果为null，订单仍将返回但用户信息为空");
                }
            } catch (Exception e) {
                log.error("远程获取用户信息失败", e);
                // 继续处理，不返回错误，只是不显示用户信息
            }
        }
        TableDataInfo tableDataInfo = TableDataInfo.success(voList, resultPage.getTotal());
        try{
            log.info("更改订单为已发货111111");
            String message = "订单发货";
            rabbitTemplate.convertAndSend("admin-order-service.topic", "order-consign",message);
        }catch (Exception e){
            log.warn("更改订单为已发货失败");
        }
        log.info("列表查询结束");
        return tableDataInfo;
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
        Long userId = AdministratorContext.getUser();
        if(userId == null){
            throw new RuntimeException("用户未登录,请检查登录信息");
        }
        Order order = lambdaQuery().eq(Order::getId, id).one();

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
        orderVO.setReceiverContact(address.getData().getContact());
        orderVO.setReceiverMobile(address.getData().getMobile());
        orderVO.setReceiverAddress(address.getData().getStreet());
        return orderVO;
    }

    @Override
    public void batchConsignOrders() {

        boolean update = lambdaUpdate()
                .eq(Order::getStatus, OrderStatus.PAID)
                .le(Order::getCreateTime, LocalDateTime.now())
                .set(Order::getStatus, OrderStatus.SHIPPED)
                .set(Order::getConsignTime, LocalDateTime.now())
                .update();
        if (!update){
            log.info("订单暂不满足发货条件（可能状态不是PAID或支付未满24小时）");
            throw new RuntimeException("订单暂不满足发货条件（可能状态不是PAID或支付未满24小时）");
        }
    }

    @Override
    public List<OrderDashBoardVO> recentOrders() {
        // 1. 查询订单列表（这里完全允许查出同一个用户的多笔订单）
        List<Order> orderList = lambdaQuery()
                .orderByDesc(Order::getCreateTime)
                .last("LIMIT 5")
                .list();

        if (CollUtil.isEmpty(orderList)) {
            return Collections.emptyList();
        }

        // 2. 获取用户信息 Map
        // 核心逻辑：这里生成的 Map 是一个"查表字典"。
        // 即使 orderList 里有 3 个订单都是 userId=100，Map 里只需要有一条 key=100 的数据即可。
        Map<Long, UserVO> userMap = getUserMapByOrders(orderList);

        return orderList.stream().map(order -> {
            OrderDashBoardVO vo = new OrderDashBoardVO();
            BeanUtil.copyProperties(order, vo);

            // 手动处理字段名不一致
            vo.setTotalPrice(order.getTotalFee());

            // 优化枚举转换
            vo.setStatus(convertOrderStatus(order.getStatus()));

            // --- 组装用户信息 ---
            // 允许多个订单 get 同一个 userId
            UserVO user = userMap.get(order.getUserId());
            if (user != null) {
                log.debug("用户信息：{}", user);
                vo.setNickName(user.getNickName());
                vo.setAvatar(user.getAvatar());
            } else {
                log.warn("用户信息为空，订单仍将返回但用户信息为空");
                vo.setNickName("未知用户");
                vo.setAvatar("");
            }

            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取用户信息 Map
     */
    private Map<Long, UserVO> getUserMapByOrders(List<Order> orders) {
        // 1. 提取 ID
        // 这里加 distinct() 仅仅是为了减少 RPC 传输的数据量（省流量、快）。
        // 哪怕这里去重了，返回的 Map 依然能被主流程里的多个重复订单共用。
        List<Long> userIds = orders.stream()
                .map(Order::getUserId)
                .filter(ObjectUtil::isNotNull)
                .distinct() // 推荐加上，即使不加逻辑也没错，但加了性能更好
                .collect(Collectors.toList());

        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyMap();
        }

        try {
            // 2. 远程调用
            AjaxResult<List<UserVO>> rpcResult = userClient.getUserInfoByIds(userIds);

            if (rpcResult != null && rpcResult.isSuccess()&& CollUtil.isNotEmpty(rpcResult.getData())) {
                // 3. 转 Map (关键点)
                log.debug("远程获取用户信息成功，userIds: {}, userList: {}", userIds, rpcResult.getData());
                return rpcResult.getData().stream()
                        .filter(user -> user.getId() != null)
                        .collect(Collectors.toMap(
                                UserVO::getId,
                                Function.identity(),
                                // 【关键】键冲突策略：保留旧值 (v1)
                                // 这一行保命代码：防止 RPC 返回的数据里本身就有重复 ID，导致报错
                                (v1, v2) -> v1
                        ));
            }
        } catch (Exception e) {
            log.error("远程获取用户信息失败，userIds: {}", userIds, e);
        }
        log.warn("远程获取用户信息失败，userIds: {}", userIds);
        return Collections.emptyMap();
    }

    /**
     * 状态转换：优化为 Map 查找，避免循环
     */
    private OrderStatus convertOrderStatus(Integer statusValue) {
        if (statusValue == null) {
            return null;
        }
        // 如果 OrderStatus 有 fromValue/getByValue 静态方法最好，没有的话可以用这个
        // 建议把这个逻辑移到 Enum 类内部，或者缓存一个静态 Map，不要每次 stream 遍历
        return Arrays.stream(OrderStatus.values())
                .filter(s -> s.getValue().equals(statusValue))
                .findFirst()
                .orElse(null);
    }
}

