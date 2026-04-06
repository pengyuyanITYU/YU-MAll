package com.yu.comment.controller;

import com.yu.comment.domain.dto.CommentRejectDTO;
import com.yu.comment.domain.query.CommentAdminPageQuery;
import com.yu.comment.domain.vo.CommentVO;
import com.yu.comment.service.ICommentService;
import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.common.exception.UnauthorizedException;
import com.yu.common.utils.AdministratorContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
@Api(tags = "评论审核管理")
public class AdminCommentController {

    private final ICommentService commentService;

    @ModelAttribute
    public void checkAdminLogin() {
        if (AdministratorContext.getUser() == null) {
            throw new UnauthorizedException("unauthorized");
        }
    }

    @GetMapping("/list")
    @ApiOperation("分页查询评论审核列表")
    public TableDataInfo list(CommentAdminPageQuery query) {
        return commentService.listAdminComments(query);
    }

    @GetMapping("/{id}")
    @ApiOperation("查询评论详情")
    public AjaxResult<CommentVO> detail(@PathVariable Long id) {
        return AjaxResult.success(commentService.getUserCommentDetail(id));
    }

    @PutMapping("/{id}/approve")
    @ApiOperation("审核通过")
    public AjaxResult<Void> approve(@PathVariable Long id) {
        commentService.approveComment(id);
        return AjaxResult.success();
    }

    @PutMapping("/{id}/reject")
    @ApiOperation("审核驳回")
    public AjaxResult<Void> reject(@PathVariable Long id, @Validated @RequestBody CommentRejectDTO rejectDTO) {
        commentService.rejectComment(id, rejectDTO);
        return AjaxResult.success();
    }
}
