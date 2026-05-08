package com.yu.search.repository;

import com.yu.search.domain.SearchItemDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface SearchItemDocumentRepository extends ElasticsearchRepository<SearchItemDocument, Long> {
}
