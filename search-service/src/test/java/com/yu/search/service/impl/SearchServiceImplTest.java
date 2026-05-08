package com.yu.search.service.impl;

import com.yu.api.client.InternalSearchItemClient;
import com.yu.api.vo.SearchItemVO;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.search.config.SearchProperties;
import com.yu.search.domain.SearchItemDocument;
import com.yu.search.repository.SearchItemDocumentRepository;
import com.yu.search.support.SearchDocumentMapper;
import com.yu.search.support.SearchQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceImplTest {

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @Mock
    private SearchItemDocumentRepository repository;

    @Mock
    private InternalSearchItemClient internalSearchItemClient;

    @Mock
    private IndexOperations indexOperations;

    private SearchServiceImpl searchService;

    @BeforeEach
    void setUp() {
        SearchProperties properties = new SearchProperties();
        properties.setRebuildPageSize(2);
        searchService = new SearchServiceImpl(
                elasticsearchOperations,
                repository,
                new SearchDocumentMapper(),
                new SearchQueryBuilder(),
                properties,
                internalSearchItemClient
        );
    }

    @Test
    void rebuild_ShouldRecreateIndexAndSaveAllPages() {
        SearchItemVO first = buildItem(1L, "A");
        SearchItemVO second = buildItem(2L, "B");

        TableDataInfo firstPage = TableDataInfo.success(List.of(first), 2L);
        TableDataInfo secondPage = TableDataInfo.success(List.of(second), 2L);
        TableDataInfo emptyPage = TableDataInfo.success(List.of(), 2L);

        when(elasticsearchOperations.indexOps(SearchItemDocument.class)).thenReturn(indexOperations);
        when(indexOperations.exists()).thenReturn(true);
        when(indexOperations.create()).thenReturn(true);
        when(indexOperations.createMapping(SearchItemDocument.class)).thenReturn(Document.create());
        when(indexOperations.putMapping(any(Document.class))).thenReturn(true);
        when(internalSearchItemClient.page(any())).thenReturn(firstPage, secondPage, emptyPage);

        searchService.rebuild();

        verify(indexOperations).delete();
        verify(indexOperations).create();
        verify(indexOperations).putMapping(any(Document.class));
        verify(repository, times(2)).saveAll(ArgumentMatchers.<Iterable<SearchItemDocument>>any());
    }

    private SearchItemVO buildItem(Long id, String name) {
        SearchItemVO item = new SearchItemVO();
        item.setId(id);
        item.setName(name);
        item.setPrice(100L);
        item.setStatus(1);
        return item;
    }
}
