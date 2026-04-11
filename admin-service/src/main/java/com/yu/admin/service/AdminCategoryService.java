package com.yu.admin.service;

import com.yu.api.client.InternalAdminCategoryClient;
import com.yu.api.dto.admin.AdminCategoryDTO;
import com.yu.api.query.admin.AdminCategoryPageQuery;
import com.yu.api.vo.admin.AdminCategoryExportVO;
import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.common.utils.ExcelUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCategoryService {

    private final InternalAdminCategoryClient internalAdminCategoryClient;

    public AjaxResult<Void> add(AdminCategoryDTO categoryDTO) {
        return internalAdminCategoryClient.add(categoryDTO);
    }

    public AjaxResult<Void> delete(Long id) {
        return internalAdminCategoryClient.delete(id);
    }

    public AjaxResult<Void> deleteByIds(List<Long> ids) {
        return internalAdminCategoryClient.deleteByIds(ids);
    }

    public AjaxResult<Void> update(AdminCategoryDTO categoryDTO) {
        return internalAdminCategoryClient.update(categoryDTO);
    }

    public AjaxResult<Object> getById(Long id) {
        return internalAdminCategoryClient.getById(id);
    }

    public TableDataInfo list(AdminCategoryPageQuery query) {
        return internalAdminCategoryClient.list(query);
    }

    public AjaxResult<Object> simpleList() {
        return internalAdminCategoryClient.simpleList();
    }

    public void export(HttpServletResponse response) {
        AjaxResult<List<AdminCategoryExportVO>> result = internalAdminCategoryClient.exportData();
        ExcelUtils.exportEasyExcel(response, result.getData(), AdminCategoryExportVO.class, "categories");
    }
}
