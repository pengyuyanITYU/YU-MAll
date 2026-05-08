package com.yu.search.support;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.SortOptions;
import com.yu.api.query.SearchItemQuery;
import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchQueryBuilderTest {

    private final SearchQueryBuilder queryBuilder = new SearchQueryBuilder();

    @Test
    void build_ShouldCreateKeywordFilterAndSorts() {
        SearchItemQuery query = new SearchItemQuery();
        query.setKeyword("手机");
        query.setCategory("电子产品");
        query.setBrand("YU");
        query.setMinPrice(1000);
        query.setMaxPrice(3000);
        query.setPageNo(2);
        query.setPageSize(10);
        query.setSortBy("price");
        query.setIsAsc(false);

        NativeQuery nativeQuery = queryBuilder.build(query);
        Query boolQuery = queryBuilder.buildQuery(query);
        List<SortOptions> sorts = queryBuilder.buildSorts(query);

        assertEquals(10, nativeQuery.getPageable().getPageSize());
        assertEquals(1, nativeQuery.getPageable().getPageNumber());
        assertTrue(boolQuery.isBool());
        assertEquals(1, boolQuery.bool().must().size());
        assertTrue(boolQuery.bool().must().get(0).isMultiMatch());
        assertEquals(3, boolQuery.bool().filter().size());
        assertEquals("price", sorts.get(0).field().field());
        assertEquals(SortOrder.Desc, sorts.get(0).field().order());
    }

    @Test
    void buildSorts_ShouldUseScoreAndSoldWhenSortMissing() {
        SearchItemQuery query = new SearchItemQuery();
        query.setKeyword("耳机");

        List<SortOptions> sorts = queryBuilder.buildSorts(query);

        assertEquals(3, sorts.size());
        assertTrue(sorts.get(0).isScore());
        assertEquals(SortOrder.Desc, sorts.get(0).score().order());
        assertEquals("sold", sorts.get(1).field().field());
        assertEquals("updateTime", sorts.get(2).field().field());
    }
}
