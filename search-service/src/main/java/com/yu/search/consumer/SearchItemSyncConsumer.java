package com.yu.search.consumer;

import com.yu.api.dto.SearchItemIndexEventDTO;
import com.yu.api.enums.SearchSyncAction;
import com.yu.search.service.ISearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SearchItemSyncConsumer {

    private final ISearchService searchService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "search-service-sync.queue", durable = "true"),
            exchange = @Exchange(value = "search-item-service.topic", durable = "true", type = "topic"),
            key = {"item.upsert", "item.delete"}
    ))
    public void consume(SearchItemIndexEventDTO event) {
        if (event == null || event.getAction() == null) {
            return;
        }
        if (event.getAction() == SearchSyncAction.DELETE) {
            searchService.delete(event.getItemId());
            return;
        }
        if (event.getAction() == SearchSyncAction.UPSERT) {
            searchService.upsert(event.getItem());
            return;
        }
        log.warn("unsupported search sync action: {}", event.getAction());
    }
}
