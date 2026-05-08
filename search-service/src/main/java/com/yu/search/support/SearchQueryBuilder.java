package com.yu.search.support;

import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import com.yu.api.query.SearchItemQuery;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class SearchQueryBuilder {

    public NativeQuery build(SearchItemQuery query) {
        SearchItemQuery actual = query == null ? new SearchItemQuery() : query;
        return NativeQuery.builder()
                .withQuery(buildQuery(actual))
                .withPageable(PageRequest.of(actual.getPageNo() - 1, actual.getPageSize()))
                .withSort(buildSorts(actual))
                .build();
    }

    Query buildQuery(SearchItemQuery query) {
        BoolQuery.Builder bool = new BoolQuery.Builder();
        if (StringUtils.hasText(query.getKeyword())) {
            bool.must(q -> q.multiMatch(m -> m
                    .query(query.getKeyword().trim())
                    .fields("name^4", "subTitle^2", "brand^2", "category")
                    .type(TextQueryType.BestFields)));
        } else {
            bool.must(q -> q.matchAll(m -> m));
        }
        if (StringUtils.hasText(query.getCategory())) {
            bool.filter(q -> q.term(t -> t.field("category").value(query.getCategory().trim())));
        }
        if (StringUtils.hasText(query.getBrand())) {
            bool.filter(q -> q.term(t -> t.field("brand").value(query.getBrand().trim())));
        }
        if (query.getMinPrice() != null || query.getMaxPrice() != null) {
            bool.filter(q -> q.range(r -> r.number(n -> {
                n.field("price");
                if (query.getMinPrice() != null) {
                    n.gte(query.getMinPrice().doubleValue());
                }
                if (query.getMaxPrice() != null) {
                    n.lte(query.getMaxPrice().doubleValue());
                }
                return n;
            })));
        }
        return new Query.Builder().bool(bool.build()).build();
    }

    List<SortOptions> buildSorts(SearchItemQuery query) {
        List<SortOptions> sorts = new ArrayList<>();
        String sortBy = query.getSortBy();
        boolean isAsc = Boolean.TRUE.equals(query.getIsAsc());
        if (!StringUtils.hasText(sortBy)) {
            sorts.add(SortOptions.of(s -> s.score(score -> score.order(SortOrder.Desc))));
            sorts.add(SortOptions.of(s -> s.field(f -> f.field("sold").order(SortOrder.Desc))));
            sorts.add(SortOptions.of(s -> s.field(f -> f.field("updateTime").order(SortOrder.Desc))));
            return sorts;
        }
        SortOrder order = isAsc ? SortOrder.Asc : SortOrder.Desc;
        sorts.add(SortOptions.of(s -> s.field(f -> f.field(sortBy).order(order))));
        sorts.add(SortOptions.of(s -> s.score(score -> score.order(SortOrder.Desc))));
        return sorts;
    }
}
