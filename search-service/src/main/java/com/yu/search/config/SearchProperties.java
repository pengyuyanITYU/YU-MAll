package com.yu.search.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component("searchProperties")
@ConfigurationProperties(prefix = "yu.search")
public class SearchProperties {

    private String indexName = "mall_item_search";

    private int rebuildPageSize = 200;
}
