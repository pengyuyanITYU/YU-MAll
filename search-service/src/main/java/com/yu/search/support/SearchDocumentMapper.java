package com.yu.search.support;

import com.yu.api.vo.SearchItemVO;
import com.yu.search.domain.SearchItemDocument;
import org.springframework.stereotype.Component;

@Component
public class SearchDocumentMapper {

    public SearchItemDocument toDocument(SearchItemVO source) {
        if (source == null || source.getId() == null) {
            return null;
        }
        SearchItemDocument target = new SearchItemDocument();
        target.setId(source.getId());
        target.setName(source.getName());
        target.setSubTitle(source.getSubTitle());
        target.setImage(source.getImage());
        target.setPrice(source.getPrice());
        target.setOriginalPrice(source.getOriginalPrice());
        target.setBrand(source.getBrand());
        target.setCategory(source.getCategory());
        target.setShopId(source.getShopId());
        target.setShopName(source.getShopName());
        target.setIsSelf(source.getIsSelf());
        target.setShippingType(source.getShippingType());
        target.setShippingFee(source.getShippingFee());
        target.setFreeShippingThreshold(source.getFreeShippingThreshold());
        target.setShippingDesc(source.getShippingDesc());
        target.setSold(source.getSold());
        target.setCommentCount(source.getCommentCount());
        target.setPositiveRate(source.getPositiveRate());
        target.setAvgScore(source.getAvgScore());
        target.setStatus(source.getStatus());
        target.setUpdateTime(source.getUpdateTime());
        return target;
    }

    public SearchItemVO toView(SearchItemDocument source) {
        if (source == null) {
            return null;
        }
        SearchItemVO target = new SearchItemVO();
        target.setId(source.getId());
        target.setName(source.getName());
        target.setSubTitle(source.getSubTitle());
        target.setImage(source.getImage());
        target.setPrice(source.getPrice());
        target.setOriginalPrice(source.getOriginalPrice());
        target.setBrand(source.getBrand());
        target.setCategory(source.getCategory());
        target.setShopId(source.getShopId());
        target.setShopName(source.getShopName());
        target.setIsSelf(source.getIsSelf());
        target.setShippingType(source.getShippingType());
        target.setShippingFee(source.getShippingFee());
        target.setFreeShippingThreshold(source.getFreeShippingThreshold());
        target.setShippingDesc(source.getShippingDesc());
        target.setSold(source.getSold());
        target.setCommentCount(source.getCommentCount());
        target.setPositiveRate(source.getPositiveRate());
        target.setAvgScore(source.getAvgScore());
        target.setStatus(source.getStatus());
        target.setUpdateTime(source.getUpdateTime());
        return target;
    }
}
