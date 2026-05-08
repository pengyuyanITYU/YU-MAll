package com.yu.item.support;

import com.yu.api.dto.SearchItemIndexEventDTO;
import com.yu.api.enums.SearchSyncAction;
import com.yu.api.vo.SearchItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@RequiredArgsConstructor
public class SearchSyncMessageProducer {

    private static final String EXCHANGE = "search-item-service.topic";
    private static final String UPSERT_ROUTING_KEY = "item.upsert";
    private static final String DELETE_ROUTING_KEY = "item.delete";

    private final RabbitTemplate rabbitTemplate;

    public void publishUpsert(SearchItemVO item) {
        if (item == null || item.getId() == null) {
            return;
        }
        SearchItemIndexEventDTO event = new SearchItemIndexEventDTO();
        event.setAction(SearchSyncAction.UPSERT);
        event.setItemId(item.getId());
        event.setItem(item);
        publishAfterCommit(UPSERT_ROUTING_KEY, event);
    }

    public void publishDelete(Long itemId) {
        if (itemId == null) {
            return;
        }
        SearchItemIndexEventDTO event = new SearchItemIndexEventDTO();
        event.setAction(SearchSyncAction.DELETE);
        event.setItemId(itemId);
        publishAfterCommit(DELETE_ROUTING_KEY, event);
    }

    private void publishAfterCommit(String routingKey, SearchItemIndexEventDTO event) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            rabbitTemplate.convertAndSend(EXCHANGE, routingKey, event);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                rabbitTemplate.convertAndSend(EXCHANGE, routingKey, event);
            }
        });
    }
}
