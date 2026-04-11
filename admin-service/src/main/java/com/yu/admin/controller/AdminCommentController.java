package com.yu.admin.controller;

import com.yu.admin.service.AdminCommentService;
import com.yu.api.dto.admin.AdminCommentRejectDTO;
import com.yu.api.query.admin.AdminCommentPageQuery;
import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/admin/comments")
public class AdminCommentController {

    private final AdminCommentService adminCommentService;

    @GetMapping("/list")
    public TableDataInfo list(AdminCommentPageQuery query) {
        return adminCommentService.list(query);
    }

    @GetMapping("/{id}")
    public AjaxResult<Object> getById(@PathVariable Long id) {
        return adminCommentService.getById(id);
    }

    @PutMapping("/{id}/approve")
    public AjaxResult<Void> approve(@PathVariable Long id) {
        return adminCommentService.approve(id);
    }

    @PutMapping("/{id}/reject")
    public AjaxResult<Void> reject(@PathVariable Long id, @Valid @RequestBody AdminCommentRejectDTO rejectDTO) {
        return adminCommentService.reject(id, rejectDTO);
    }
}
