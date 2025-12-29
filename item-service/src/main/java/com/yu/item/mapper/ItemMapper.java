package com.yu.item.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yu.item.domain.po.Item;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ItemMapper extends BaseMapper<Item> {

    /**
     * 统计各分类下的商品数量
     * @return List<Map<String, Object>> name=分类名, value=数量
     */
    @Select("SELECT category as name, COUNT(*) as value FROM item GROUP BY category")
    List<Map<String, Object>> selectCategoryDistribution();

    /**
     * 查询销量最高的商品 Top 10
     * @return List<Map<String, Object>> name=商品名, value=销量
     */
    @Select("SELECT name, sold as value FROM item ORDER BY sold DESC LIMIT 10")
    List<Map<String, Object>> selectTopSelling();
    
    /**
     * 统计库存紧张的商品数量 (假设库存小于10为紧张)
     */
    @Select("SELECT COUNT(*) FROM item WHERE stock < 10")
    Long countLowStock();
}