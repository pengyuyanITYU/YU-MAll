package com.yu.admin.service;

import com.yu.api.client.InternalAdminCommentClient;
import com.yu.api.dto.admin.AdminCommentRejectDTO;
import com.yu.api.query.admin.AdminCommentPageQuery;
import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminCommentService {

    private final InternalAdminCommentClient internalAdminCommentClient;

    public TableDataInfo list(AdminCommentPageQuery query) {
        return internalAdminCommentClient.list(query);
    }

    public AjaxResult<Object> getById(Long id) {
        return internalAdminCommentClient.getById(id);
    }

    public AjaxResult<Void> approve(Long id) {
        return internalAdminCommentClient.approve(id);
    }

    public AjaxResult<Void> reject(Long id, AdminCommentRejectDTO rejectDTO) {
        return internalAdminCommentClient.reject(id, rejectDTO);
    }
}
