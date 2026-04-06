package com.yu.comment.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.yu.api.client.ItemClient;
import com.yu.api.client.OrderClient;
import com.yu.api.client.UserClient;
import com.yu.api.vo.ItemDetailVO;
import com.yu.comment.domain.dto.CommentDTO;
import com.yu.comment.domain.po.Comment;
import com.yu.comment.domain.vo.CommentVO;
import com.yu.comment.domain.vo.CommentsVO;
import com.yu.comment.service.ICommentLikeService;
import com.yu.common.domain.AjaxResult;
import com.yu.common.exception.BusinessException;
import com.yu.common.utils.UserContext;
import com.yu.item.mapper.ItemMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private ICommentLikeService commentLikeService;

    @Mock
    private UserClient userClient;

    @Mock
    private OrderClient orderClient;

    @Mock
    private ItemClient itemClient;

    @Mock
    private ItemMapper itemMapper;

    @Mock(answer = Answers.RETURNS_SELF)
    private LambdaQueryChainWrapper<Comment> lambdaQueryWrapper;

    @Mock(answer = Answers.RETURNS_SELF)
    private LambdaUpdateChainWrapper<Comment> lambdaUpdateWrapper;

    private CommentServiceImpl commentService;

    @BeforeEach
    void setUp() {
        commentService = spy(new CommentServiceImpl(commentLikeService, userClient, orderClient, itemClient, itemMapper));
        UserContext.setUser(66L);
    }

    @AfterEach
    void tearDown() {
        UserContext.removeUser();
    }

    @Test
    void getMyComments_shouldNotFallbackToFirstSku_whenCommentSkuDoesNotMatch() {
        doReturn(lambdaQueryWrapper).when(commentService).lambdaQuery();
        when(lambdaQueryWrapper.eq(any(), any())).thenReturn(lambdaQueryWrapper);
        when(lambdaQueryWrapper.list()).thenReturn(List.of(buildComment()));
        when(itemClient.getItemByIds(any())).thenReturn(AjaxResult.success(List.of(buildItemDetail())));

        List<CommentsVO> comments = commentService.getMyComments();

        assertEquals(1, comments.size());
        assertNull(comments.get(0).getSkuSpecs());
        assertNull(comments.get(0).getItemImage());
    }

    @Test
    void getMyComments_shouldSortByUpdateTimeDesc() {
        Comment olderComment = buildComment();
        olderComment.setId(1L);
        olderComment.setUpdateTime(LocalDateTime.of(2026, 4, 5, 10, 0));

        Comment newerComment = buildComment();
        newerComment.setId(2L);
        newerComment.setUpdateTime(LocalDateTime.of(2026, 4, 6, 10, 0));

        doReturn(lambdaQueryWrapper).when(commentService).lambdaQuery();
        when(lambdaQueryWrapper.eq(any(), any())).thenReturn(lambdaQueryWrapper);
        when(lambdaQueryWrapper.list()).thenReturn(List.of(olderComment, newerComment));
        when(itemClient.getItemByIds(any())).thenReturn(AjaxResult.success(List.of(buildItemDetail())));

        List<CommentsVO> comments = commentService.getMyComments();

        assertEquals(List.of(2L, 1L), comments.stream().map(CommentsVO::getId).toList());
    }

    @Test
    void listComments_shouldOnlyReturnApprovedComments() {
        doReturn(lambdaQueryWrapper).when(commentService).lambdaQuery();
        when(lambdaQueryWrapper.eq(any(), any())).thenReturn(lambdaQueryWrapper);
        when(lambdaQueryWrapper.eq(anyBoolean(), any(), any())).thenReturn(lambdaQueryWrapper);
        when(lambdaQueryWrapper.list()).thenReturn(List.of(buildApprovedComment(), buildPendingComment()));
        when(itemClient.getItemByIds(any())).thenReturn(AjaxResult.success(List.of(buildItemDetail())));

        List<CommentsVO> comments = commentService.listComments(200L);

        assertEquals(1, comments.size());
        assertEquals(1L, comments.get(0).getId());
    }

    @Test
    void listComments_shouldSortByUpdateTimeDesc() {
        Comment olderComment = buildApprovedComment();
        olderComment.setId(1L);
        olderComment.setUpdateTime(LocalDateTime.of(2026, 4, 5, 10, 0));

        Comment newerComment = buildApprovedComment();
        newerComment.setId(2L);
        newerComment.setUpdateTime(LocalDateTime.of(2026, 4, 6, 10, 0));

        doReturn(lambdaQueryWrapper).when(commentService).lambdaQuery();
        when(lambdaQueryWrapper.eq(any(), any())).thenReturn(lambdaQueryWrapper);
        when(lambdaQueryWrapper.eq(anyBoolean(), any(), any())).thenReturn(lambdaQueryWrapper);
        when(lambdaQueryWrapper.list()).thenReturn(List.of(olderComment, newerComment));
        when(itemClient.getItemByIds(any())).thenReturn(AjaxResult.success(List.of(buildItemDetail())));

        List<CommentsVO> comments = commentService.listComments(200L);

        assertEquals(List.of(2L, 1L), comments.stream().map(CommentsVO::getId).toList());
    }

    @Test
    void comment_shouldSaveNewCommentAsPendingReview() {
        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        doAnswer(invocation -> true).when(commentService).save(commentCaptor.capture());

        commentService.comment(buildCommentDTO());

        assertEquals(0, commentCaptor.getValue().getStatus());
        assertEquals(66L, commentCaptor.getValue().getUserId());
        verify(orderClient).updateOrderCommented(any(com.yu.api.dto.CommentDTO.class));
    }

    @Test
    void resubmitRejectedComment_shouldResetStatusAndRejectReason() {
        Comment rejectedComment = buildComment();
        rejectedComment.setStatus(2);
        rejectedComment.setRejectReason("invalid content");
        rejectedComment.setUserId(66L);

        doReturn(lambdaQueryWrapper).when(commentService).lambdaQuery();
        doReturn(lambdaUpdateWrapper).when(commentService).lambdaUpdate();
        doReturn(true).when(commentService).updateById(any(Comment.class));
        when(lambdaQueryWrapper.eq(any(), any())).thenReturn(lambdaQueryWrapper);
        when(lambdaQueryWrapper.one()).thenReturn(rejectedComment);
        when(lambdaUpdateWrapper.eq(any(), any())).thenReturn(lambdaUpdateWrapper);
        when(lambdaUpdateWrapper.set(any(), any())).thenReturn(lambdaUpdateWrapper);
        when(lambdaUpdateWrapper.update()).thenReturn(true);

        CommentDTO commentDTO = buildCommentDTO();
        commentDTO.setContent("updated content");
        commentDTO.setImages(List.of("proof.png"));

        commentService.resubmitComment(1L, commentDTO);

        verify(lambdaUpdateWrapper).set(any(), isNull());
        verify(lambdaUpdateWrapper).update();
    }

    @Test
    void resubmitRejectedComment_shouldUpdateImagesViaEntityUpdate() {
        Comment rejectedComment = buildComment();
        rejectedComment.setStatus(2);
        rejectedComment.setRejectReason("invalid image");
        rejectedComment.setUserId(66L);

        doReturn(lambdaQueryWrapper).when(commentService).lambdaQuery();
        doReturn(true).when(commentService).updateById(any(Comment.class));
        doReturn(lambdaUpdateWrapper).when(commentService).lambdaUpdate();
        when(lambdaQueryWrapper.eq(any(), any())).thenReturn(lambdaQueryWrapper);
        when(lambdaQueryWrapper.one()).thenReturn(rejectedComment);
        when(lambdaUpdateWrapper.eq(any(), any())).thenReturn(lambdaUpdateWrapper);
        when(lambdaUpdateWrapper.set(any(), any())).thenReturn(lambdaUpdateWrapper);
        when(lambdaUpdateWrapper.update()).thenReturn(true);

        CommentDTO commentDTO = buildCommentDTO();
        commentDTO.setContent("updated content");
        commentDTO.setImages(List.of("proof.png"));

        commentService.resubmitComment(1L, commentDTO);

        ArgumentCaptor<Comment> updateCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentService).updateById(updateCaptor.capture());
        assertEquals(List.of("proof.png"), updateCaptor.getValue().getImages());
        assertEquals(0, updateCaptor.getValue().getStatus());
        verify(lambdaUpdateWrapper).set(any(), isNull());
    }

    @Test
    void resubmitRejectedComment_shouldRejectNonRejectedStatus() {
        Comment approvedComment = buildComment();
        approvedComment.setStatus(1);
        approvedComment.setUserId(66L);

        doReturn(lambdaQueryWrapper).when(commentService).lambdaQuery();
        when(lambdaQueryWrapper.eq(any(), any())).thenReturn(lambdaQueryWrapper);
        when(lambdaQueryWrapper.one()).thenReturn(approvedComment);

        assertThrows(BusinessException.class, () -> commentService.resubmitComment(1L, buildCommentDTO()));
    }

    @Test
    void getMyCommentDetail_shouldFilterByCurrentUser() {
        Comment otherUsersComment = buildComment();
        otherUsersComment.setUserId(77L);

        doReturn(lambdaQueryWrapper).when(commentService).lambdaQuery();
        when(lambdaQueryWrapper.eq(anyBoolean(), any(), any())).thenReturn(lambdaQueryWrapper);
        when(lambdaQueryWrapper.eq(any(), any())).thenReturn(lambdaQueryWrapper);
        when(lambdaQueryWrapper.one()).thenReturn(otherUsersComment);

        CommentVO detail = commentService.getMyCommentDetail(1L);

        assertNotNull(detail);
        verify(lambdaQueryWrapper).eq(any(), org.mockito.ArgumentMatchers.eq(66L));
    }

    private static Comment buildComment() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setUserId(66L);
        comment.setItemId(200L);
        comment.setSkuId(999L);
        comment.setContent("good");
        comment.setRating(5);
        comment.setStatus(1);
        comment.setCreateTime(LocalDateTime.of(2026, 4, 5, 9, 0));
        comment.setUpdateTime(LocalDateTime.of(2026, 4, 5, 9, 0));
        return comment;
    }

    private static Comment buildApprovedComment() {
        return buildComment();
    }

    private static Comment buildPendingComment() {
        Comment comment = buildComment();
        comment.setId(3L);
        comment.setStatus(0);
        return comment;
    }

    private static ItemDetailVO buildItemDetail() {
        ItemDetailVO itemDetailVO = new ItemDetailVO();
        itemDetailVO.setId(200L);
        itemDetailVO.setName("test item");

        ItemDetailVO.SkuVO whiteSku = new ItemDetailVO.SkuVO();
        whiteSku.setId(11L);
        whiteSku.setImage("white.png");
        whiteSku.setSpecs(Map.of("color", "white"));

        ItemDetailVO.SkuVO blackSku = new ItemDetailVO.SkuVO();
        blackSku.setId(12L);
        blackSku.setImage("black.png");
        blackSku.setSpecs(Map.of("color", "black"));

        itemDetailVO.setSkuList(List.of(whiteSku, blackSku));
        return itemDetailVO;
    }

    private static CommentDTO buildCommentDTO() {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setItemId(200L);
        commentDTO.setSkuId(12L);
        commentDTO.setOrderId(300L);
        commentDTO.setOrderDetailId(301L);
        commentDTO.setRating(5);
        commentDTO.setContent("good");
        commentDTO.setIsAnonymous(false);
        commentDTO.setUserNickname("tester");
        commentDTO.setUserAvatar("avatar.png");
        return commentDTO;
    }
}
