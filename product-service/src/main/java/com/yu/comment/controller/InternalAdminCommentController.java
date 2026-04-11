package com.yu.comment.controller;

import com.yu.api.dto.admin.AdminCommentRejectDTO;
import com.yu.api.query.admin.AdminCommentPageQuery;
import com.yu.comment.domain.dto.CommentRejectDTO;
import com.yu.comment.domain.query.CommentAdminPageQuery;
import com.yu.comment.service.ICommentService;
import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/admin/comments")
public class InternalAdminCommentController {

    private final ICommentService commentService;

    @GetMapping("/list")
    public TableDataInfo list(AdminCommentPageQuery query) {
        CommentAdminPageQuery target = new CommentAdminPageQuery();
        if (query != null) {
            BeanUtils.copyProperties(query, target);
        }
        return commentService.listAdminComments(target);
    }

    @GetMapping("/{id}")
    public AjaxResult<Object> detail(@PathVariable Long id) {
        return AjaxResult.success(commentService.getUserCommentDetail(id));
    }

    @PutMapping("/{id}/approve")
    public AjaxResult<Void> approve(@PathVariable Long id) {
        commentService.approveComment(id);
        return AjaxResult.success();
    }

    @PutMapping("/{id}/reject")
    public AjaxResult<Void> reject(@PathVariable Long id, @Validated @RequestBody AdminCommentRejectDTO rejectDTO) {
        CommentRejectDTO target = new CommentRejectDTO();
        BeanUtils.copyProperties(rejectDTO, target);
        commentService.rejectComment(id, target);
        return AjaxResult.success();
    }
}
