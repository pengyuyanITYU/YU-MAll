package com.yu.comment.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yu.api.client.ItemClient;
import com.yu.api.client.OrderClient;
import com.yu.api.client.UserClient;
import com.yu.api.vo.ItemDetailVO;
import com.yu.comment.constants.LikeType;
import com.yu.comment.domain.dto.CommentDTO;
import com.yu.comment.domain.dto.CommentRejectDTO;
import com.yu.comment.domain.po.Comment;
import com.yu.comment.domain.query.CommentAdminPageQuery;
import com.yu.comment.domain.vo.CommentVO;
import com.yu.comment.domain.vo.CommentsVO;
import com.yu.comment.mapper.CommentMapper;
import com.yu.comment.service.ICommentLikeService;
import com.yu.comment.service.ICommentService;
import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.common.exception.BusinessException;
import com.yu.common.utils.BeanUtils;
import com.yu.common.utils.CollUtils;
import com.yu.common.utils.UserContext;
import com.yu.item.domain.po.Item;
import com.yu.item.mapper.ItemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    private static final String ANONYMOUS_USER = "匿名用户";

    private final ICommentLikeService commentLikeService;
    private final UserClient userClient;
    private final OrderClient orderClient;
    private final ItemClient itemClient;
    private final ItemMapper itemMapper;

    @Override
    public List<CommentsVO> listComments(Long id) {
        List<Comment> comments = lambdaQuery()
                .eq(id != null, Comment::getItemId, id)
                .eq(Comment::getStatus, 1)
                .list();
        if (CollUtils.isEmpty(comments)) {
            return Collections.emptyList();
        }
        List<Comment> sortedComments = sortCommentsByUpdateTimeDesc(comments);
        Map<Long, ItemDetailVO> itemMap = loadItemDetailMap(sortedComments);
        return sortedComments.stream()
                .filter(comment -> Objects.equals(comment.getStatus(), 1))
                .map(comment -> buildCommentView(comment, itemMap))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void comment(CommentDTO commentDTO) {
        Long userId = UserContext.getUser();
        log.info("user {} publish comment {}", userId, commentDTO);
        Comment comment = BeanUtils.copyBean(commentDTO, Comment.class);
        fillUserSnapshot(comment, commentDTO, userId);
        comment.setStatus(0);
        comment.setRejectReason(null);
        if (!save(comment)) {
            throw new BusinessException("保存评论失败");
        }
        com.yu.api.dto.CommentDTO sharedCommentDTO = BeanUtils.copyBean(commentDTO, com.yu.api.dto.CommentDTO.class);
        orderClient.updateOrderCommented(sharedCommentDTO);
    }

    @Override
    public void deleteMyComment(Long id) {
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }
        Comment comment = lambdaQuery()
                .eq(Comment::getUserId, userId)
                .eq(Comment::getId, id)
                .one();
        if (comment == null) {
            return;
        }
        removeById(id);
    }

    @Override
    public List<CommentsVO> getMyComments() {
        Long userId = UserContext.getUser();
        List<Comment> comments = lambdaQuery()
                .eq(Comment::getUserId, userId)
                .list();
        if (CollUtils.isEmpty(comments)) {
            return Collections.emptyList();
        }
        List<Comment> sortedComments = sortCommentsByUpdateTimeDesc(comments);
        Map<Long, ItemDetailVO> itemMap = loadItemDetailMap(sortedComments);
        return sortedComments.stream()
                .map(comment -> buildCommentView(comment, itemMap))
                .collect(Collectors.toList());
    }

    @Override
    public void likeComment(Long id) {
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }
        Integer likeType = commentLikeService.getByCommentId(id);
        Comment comment = lambdaQuery().eq(Comment::getId, id).one();
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        if (Objects.equals(likeType, LikeType.LIKE)) {
            commentLikeService.removeByCommentId(id);
            boolean update = lambdaUpdate().eq(Comment::getId, id).setSql("like_count = like_count - 1").update();
            if (!update) {
                throw new BusinessException("取消点赞失败");
            }
            return;
        }
        commentLikeService.likeComment(id, comment.getItemId(), comment.getSkuId());
        boolean update = lambdaUpdate().eq(Comment::getId, id).setSql("like_count = like_count + 1").update();
        if (!update) {
            throw new BusinessException("点赞失败");
        }
    }

    @Override
    public CommentVO getUserCommentDetail(Long id) {
        Comment comment = lambdaQuery().eq(id != null, Comment::getId, id).one();
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        CommentVO vo = BeanUtils.copyBean(comment, CommentVO.class);
        Map<Long, ItemDetailVO> itemMap = loadItemDetailMap(Collections.singletonList(comment));
        ItemDetailVO itemDetail = itemMap.get(comment.getItemId());
        if (itemDetail != null) {
            vo.setItemName(itemDetail.getName());
            fillSkuView(comment, itemDetail, vo);
        }
        return vo;
    }

    @Override
    public CommentVO getMyCommentDetail(Long id) {
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }
        Comment comment = lambdaQuery()
                .eq(id != null, Comment::getId, id)
                .eq(Comment::getUserId, userId)
                .one();
        if (comment == null) {
            throw new BusinessException("评论不存在或无权查看");
        }
        CommentVO vo = BeanUtils.copyBean(comment, CommentVO.class);
        Map<Long, ItemDetailVO> itemMap = loadItemDetailMap(Collections.singletonList(comment));
        ItemDetailVO itemDetail = itemMap.get(comment.getItemId());
        if (itemDetail != null) {
            vo.setItemName(itemDetail.getName());
            fillSkuView(comment, itemDetail, vo);
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resubmitComment(Long id, CommentDTO commentDTO) {
        Long userId = UserContext.getUser();
        Comment comment = lambdaQuery()
                .eq(Comment::getId, id)
                .eq(Comment::getUserId, userId)
                .one();
        if (comment == null) {
            throw new BusinessException("评论不存在或无权操作");
        }
        if (!Objects.equals(comment.getStatus(), 2)) {
            throw new BusinessException("只有已驳回评论才可以重新提交");
        }

        comment.setRating(commentDTO.getRating());
        comment.setContent(commentDTO.getContent());
        comment.setImages(commentDTO.getImages());
        comment.setIsAnonymous(Boolean.TRUE.equals(commentDTO.getIsAnonymous()));
        comment.setUserAvatar(commentDTO.getUserAvatar());
        comment.setUserNickname(Boolean.TRUE.equals(commentDTO.getIsAnonymous()) ? ANONYMOUS_USER : commentDTO.getUserNickname());
        comment.setStatus(0);

        if (!updateById(comment)) {
            throw new BusinessException("重新提交评论失败");
        }
        boolean cleared = lambdaUpdate()
                .eq(Comment::getId, id)
                .eq(Comment::getUserId, userId)
                .set(Comment::getRejectReason, null)
                .update();
        if (!cleared) {
            throw new BusinessException("重新提交评论失败");
        }
    }

    @Override
    public TableDataInfo listAdminComments(CommentAdminPageQuery query) {
        CommentAdminPageQuery realQuery = query == null ? new CommentAdminPageQuery() : query;
        Page<Comment> page = realQuery.toMpPageDefaultSortByCreateTimeDesc();
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<Comment>()
                .like(StrUtil.isNotBlank(realQuery.getUserNickname()), Comment::getUserNickname, realQuery.getUserNickname())
                .eq(realQuery.getStatus() != null, Comment::getStatus, realQuery.getStatus());
        if (StrUtil.isNotBlank(realQuery.getItemName())) {
            List<Long> matchedItemIds = itemMapper.selectList(new LambdaQueryWrapper<Item>()
                            .like(Item::getName, realQuery.getItemName())
                            .select(Item::getId))
                    .stream()
                    .map(Item::getId)
                    .collect(Collectors.toList());
            if (CollUtils.isEmpty(matchedItemIds)) {
                return TableDataInfo.success(Collections.emptyList(), 0L);
            }
            wrapper.in(Comment::getItemId, matchedItemIds);
        }
        Page<Comment> result = page(page, wrapper);
        if (CollUtils.isEmpty(result.getRecords())) {
            return TableDataInfo.success(Collections.emptyList(), result.getTotal());
        }
        Map<Long, ItemDetailVO> itemMap = loadItemDetailMap(result.getRecords());
        List<CommentsVO> rows = result.getRecords().stream()
                .map(comment -> buildCommentView(comment, itemMap))
                .collect(Collectors.toList());
        return TableDataInfo.success(rows, result.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveComment(Long id) {
        Comment comment = getRequiredComment(id);
        comment.setStatus(1);
        comment.setRejectReason(null);
        if (!updateById(comment)) {
            throw new BusinessException("审核通过失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectComment(Long id, CommentRejectDTO rejectDTO) {
        if (rejectDTO == null || StrUtil.isBlank(rejectDTO.getRejectReason())) {
            throw new BusinessException("驳回原因不能为空");
        }
        Comment comment = getRequiredComment(id);
        comment.setStatus(2);
        comment.setRejectReason(rejectDTO.getRejectReason().trim());
        if (!updateById(comment)) {
            throw new BusinessException("审核驳回失败");
        }
    }

    private void fillUserSnapshot(Comment comment, CommentDTO commentDTO, Long userId) {
        comment.setUserId(userId);
        comment.setUserAvatar(commentDTO.getUserAvatar());
        comment.setUserNickname(Boolean.TRUE.equals(commentDTO.getIsAnonymous()) ? ANONYMOUS_USER : commentDTO.getUserNickname());
    }

    private Map<Long, ItemDetailVO> loadItemDetailMap(List<Comment> comments) {
        Set<Long> itemIds = comments.stream().map(Comment::getItemId).collect(Collectors.toSet());
        if (CollUtils.isEmpty(itemIds)) {
            return Collections.emptyMap();
        }
        AjaxResult<List<ItemDetailVO>> itemDatas = itemClient.getItemByIds(new ArrayList<>(itemIds));
        if (itemDatas == null || !itemDatas.isSuccess() || CollUtils.isEmpty(itemDatas.getData())) {
            return Collections.emptyMap();
        }
        return itemDatas.getData().stream()
                .collect(Collectors.toMap(ItemDetailVO::getId, Function.identity(), (left, right) -> left));
    }

    private CommentsVO buildCommentView(Comment comment, Map<Long, ItemDetailVO> itemMap) {
        CommentsVO vo = BeanUtils.copyBean(comment, CommentsVO.class);
        ItemDetailVO itemDetail = itemMap.get(comment.getItemId());
        if (itemDetail == null) {
            return vo;
        }
        vo.setItemName(itemDetail.getName());
        fillSkuView(comment, itemDetail, vo);
        return vo;
    }

    private void fillSkuView(Comment comment, ItemDetailVO itemDetail, CommentsVO vo) {
        if (CollUtils.isEmpty(itemDetail.getSkuList()) || comment.getSkuId() == null || comment.getSkuId() <= 0) {
            return;
        }
        ItemDetailVO.SkuVO targetSku = itemDetail.getSkuList().stream()
                .filter(sku -> Objects.equals(sku.getId(), comment.getSkuId()))
                .findFirst()
                .orElse(null);
        if (targetSku == null) {
            return;
        }
        vo.setItemImage(targetSku.getImage());
        vo.setSkuSpecs(targetSku.getSpecs());
    }

    private void fillSkuView(Comment comment, ItemDetailVO itemDetail, CommentVO vo) {
        if (CollUtils.isEmpty(itemDetail.getSkuList()) || comment.getSkuId() == null || comment.getSkuId() <= 0) {
            return;
        }
        ItemDetailVO.SkuVO targetSku = itemDetail.getSkuList().stream()
                .filter(sku -> Objects.equals(sku.getId(), comment.getSkuId()))
                .findFirst()
                .orElse(null);
        if (targetSku == null) {
            return;
        }
        vo.setItemImage(targetSku.getImage());
        vo.setSkuSpecs(targetSku.getSpecs());
    }

    private Comment getRequiredComment(Long id) {
        Comment comment = lambdaQuery().eq(Comment::getId, id).one();
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        return comment;
    }

    private List<Comment> sortCommentsByUpdateTimeDesc(List<Comment> comments) {
        return comments.stream()
                .sorted(Comparator
                        .comparing(this::resolveCommentSortTime, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Comment::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    private LocalDateTime resolveCommentSortTime(Comment comment) {
        if (comment == null) {
            return null;
        }
        return comment.getUpdateTime() != null ? comment.getUpdateTime() : comment.getCreateTime();
    }
}
