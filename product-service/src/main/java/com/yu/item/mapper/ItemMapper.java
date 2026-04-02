package com.yu.item.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yu.item.domain.po.Item;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ItemMapper extends BaseMapper<Item> {

    @Select("SELECT category AS name, COUNT(*) AS value FROM item GROUP BY category")
    List<Map<String, Object>> selectCategoryDistribution();

    @Select("SELECT name, sold AS value FROM item ORDER BY sold DESC LIMIT 10")
    List<Map<String, Object>> selectTopSelling();

    @Select("SELECT COUNT(*) FROM item WHERE stock < 10")
    Long countLowStock();
}
