package com.yu.search.consumer;

import com.yu.api.dto.SearchItemIndexEventDTO;
import com.yu.api.enums.SearchSyncAction;
import com.yu.api.vo.SearchItemVO;
import com.yu.search.service.ISearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SearchItemSyncConsumerTest {

    @Mock
    private ISearchService searchService;

    @InjectMocks
    private SearchItemSyncConsumer consumer;

    @Test
    void consume_ShouldDispatchUpsertEvent() {
        SearchItemVO item = new SearchItemVO();
        item.setId(1L);

        SearchItemIndexEventDTO event = new SearchItemIndexEventDTO();
        event.setAction(SearchSyncAction.UPSERT);
        event.setItemId(1L);
        event.setItem(item);

        consumer.consume(event);

        verify(searchService).upsert(item);
    }

    @Test
    void consume_ShouldDispatchDeleteEvent() {
        SearchItemIndexEventDTO event = new SearchItemIndexEventDTO();
        event.setAction(SearchSyncAction.DELETE);
        event.setItemId(2L);

        consumer.consume(event);

        verify(searchService).delete(2L);
    }
}
