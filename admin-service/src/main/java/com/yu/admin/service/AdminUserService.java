package com.yu.admin.service;

import com.yu.api.client.InternalAdminUserClient;
import com.yu.api.query.admin.AdminUserPageQuery;
import com.yu.api.vo.admin.AdminUserExportVO;
import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.common.utils.ExcelUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final InternalAdminUserClient internalAdminUserClient;

    public TableDataInfo list(AdminUserPageQuery query) {
        return internalAdminUserClient.list(query);
    }

    public AjaxResult<Object> getById(Long id) {
        return internalAdminUserClient.getById(id);
    }

    public AjaxResult<?> getByIds(List<Long> ids) {
        return internalAdminUserClient.getByIds(ids);
    }

    public AjaxResult<Void> updateStatus(Long id, Integer status) {
        return internalAdminUserClient.updateStatus(id, status);
    }

    public void export(HttpServletResponse response) {
        AjaxResult<List<AdminUserExportVO>> result = internalAdminUserClient.exportData();
        ExcelUtils.exportEasyExcel(response, result.getData(), AdminUserExportVO.class, "users");
    }
}
