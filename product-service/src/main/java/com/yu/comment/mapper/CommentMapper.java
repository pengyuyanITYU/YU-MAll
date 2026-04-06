package com.yu.comment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yu.comment.domain.po.Comment;
import com.yu.item.domain.vo.ItemCommentStatsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    @Select({
            "<script>",
            "SELECT item_id AS itemId,",
            "       SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS approvedCount,",
            "       SUM(CASE WHEN status = 1 AND rating &gt;= 4 THEN 1 ELSE 0 END) AS positiveCount",
            "FROM item_comment",
            "WHERE item_id IN",
            "<foreach collection='itemIds' item='itemId' open='(' separator=',' close=')'>",
            "  #{itemId}",
            "</foreach>",
            "GROUP BY item_id",
            "</script>"
    })
    List<ItemCommentStatsVO> selectItemStatsByItemIds(@Param("itemIds") List<Long> itemIds);
}
