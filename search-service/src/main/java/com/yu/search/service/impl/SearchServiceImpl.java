package com.yu.search.service.impl;

import com.yu.api.client.InternalSearchItemClient;
import com.yu.api.query.SearchItemQuery;
import com.yu.api.vo.SearchItemVO;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.common.domain.query.PageQuery;
import com.yu.common.utils.BeanUtils;
import com.yu.search.config.SearchProperties;
import com.yu.search.domain.SearchItemDocument;
import com.yu.search.repository.SearchItemDocumentRepository;
import com.yu.search.service.ISearchService;
import com.yu.search.support.SearchDocumentMapper;
import com.yu.search.support.SearchQueryBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchServiceImpl implements ISearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final SearchItemDocumentRepository repository;
    private final SearchDocumentMapper documentMapper;
    private final SearchQueryBuilder queryBuilder;
    private final SearchProperties searchProperties;
    private final InternalSearchItemClient internalSearchItemClient;

    @Override
    public TableDataInfo search(SearchItemQuery query) {
        ensureIndex();
        NativeQuery nativeQuery = queryBuilder.build(query);
        SearchHits<SearchItemDocument> hits = elasticsearchOperations.search(nativeQuery, SearchItemDocument.class);
        List<SearchItemVO> rows = hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(documentMapper::toView)
                .toList();
        return TableDataInfo.success(rows, hits.getTotalHits());
    }

    @Override
    public void rebuild() {
        recreateIndex();
        long total = 0L;
        int pageNo = 1;
        while (true) {
            PageQuery query = new PageQuery()
                    .setPageNo(pageNo)
                    .setPageSize(searchProperties.getRebuildPageSize())
                    .setSortBy("id")
                    .setIsAsc(true);
            TableDataInfo page = internalSearchItemClient.page(query);
            List<SearchItemVO> rows = castRows(page == null ? null : page.getRows());
            if (CollectionUtils.isEmpty(rows)) {
                break;
            }
            repository.saveAll(rows.stream().map(documentMapper::toDocument).toList());
            total += rows.size();
            if (page == null || total >= page.getTotal()) {
                break;
            }
            pageNo++;
        }
        log.info("search index rebuild finished, total={}", total);
    }

    @Override
    public void upsert(SearchItemVO item) {
        if (item == null || item.getId() == null) {
            return;
        }
        ensureIndex();
        repository.save(documentMapper.toDocument(item));
    }

    @Override
    public void delete(Long itemId) {
        if (itemId == null) {
            return;
        }
        ensureIndex();
        repository.deleteById(itemId);
    }

    private void ensureIndex() {
        IndexOperations indexOperations = elasticsearchOperations.indexOps(SearchItemDocument.class);
        if (indexOperations.exists()) {
            return;
        }
        indexOperations.create();
        indexOperations.putMapping(indexOperations.createMapping(SearchItemDocument.class));
    }

    private void recreateIndex() {
        IndexOperations indexOperations = elasticsearchOperations.indexOps(SearchItemDocument.class);
        if (indexOperations.exists()) {
            indexOperations.delete();
        }
        indexOperations.create();
        indexOperations.putMapping(indexOperations.createMapping(SearchItemDocument.class));
    }

    @SuppressWarnings("unchecked")
    private List<SearchItemVO> castRows(List<?> rows) {
        if (rows == null) {
            return Collections.emptyList();
        }
        return rows.stream()
                .map(row -> BeanUtils.copyBean(row, SearchItemVO.class))
                .toList();
    }
}
