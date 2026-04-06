package com.yu.comment.controller;

import com.yu.comment.domain.dto.CommentDTO;
import com.yu.comment.domain.vo.CommentVO;
import com.yu.comment.domain.vo.CommentsVO;
import com.yu.comment.service.ICommentService;
import com.yu.common.domain.AjaxResult;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController {

    private final ICommentService commentService;

    @ApiOperation("查询商品公开评论")
    @GetMapping("/all/{id}")
    public AjaxResult<List<CommentsVO>> listComments(@PathVariable Long id) {
        return AjaxResult.success(commentService.listComments(id));
    }

    @ApiOperation("发布评论")
    @PostMapping
    public AjaxResult<Void> comment(@Validated @RequestBody CommentDTO commentDTO) {
        commentService.comment(commentDTO);
        return AjaxResult.success();
    }

    @ApiOperation("修改驳回评论并重新提交")
    @PutMapping("/{id}")
    public AjaxResult<Void> resubmit(@PathVariable Long id, @Validated @RequestBody CommentDTO commentDTO) {
        commentService.resubmitComment(id, commentDTO);
        return AjaxResult.success();
    }

    @ApiOperation("删除我的评论")
    @ApiImplicitParam(name = "id", value = "评论ID")
    @DeleteMapping("/{id}")
    public AjaxResult<Void> deleteMyComment(@PathVariable Long id) {
        commentService.deleteMyComment(id);
        return AjaxResult.success();
    }

    @ApiOperation("查询我的评论")
    @GetMapping("/user")
    public AjaxResult<List<CommentsVO>> getMyComments() {
        return AjaxResult.success(commentService.getMyComments());
    }

    @ApiOperation("点赞评论")
    @ApiImplicitParam(name = "id", value = "评论ID")
    @PostMapping("/like/{id}")
    public AjaxResult<Void> likeComment(@PathVariable Long id) {
        commentService.likeComment(id);
        return AjaxResult.success();
    }

    @ApiOperation("查询评论详情")
    @GetMapping("/detail/{id}")
    public AjaxResult<CommentVO> getUserCommentDetail(@PathVariable Long id) {
        return AjaxResult.success(commentService.getMyCommentDetail(id));
    }
}
