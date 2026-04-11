package com.yu.admin.service;

import com.yu.api.client.InternalAdminItemClient;
import com.yu.api.dto.admin.AdminItemDTO;
import com.yu.api.query.admin.AdminItemPageQuery;
import com.yu.api.vo.admin.AdminItemExportVO;
import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.common.utils.ExcelUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminItemService {

    private final InternalAdminItemClient internalAdminItemClient;

    public TableDataInfo list(AdminItemPageQuery query) {
        return internalAdminItemClient.list(query);
    }

    public AjaxResult<Object> getById(Long id) {
        return internalAdminItemClient.getById(id);
    }

    public AjaxResult<Object> getByIds(List<Long> ids) {
        return internalAdminItemClient.getByIds(ids);
    }

    public AjaxResult<Void> delete(Long id) {
        return internalAdminItemClient.delete(id);
    }

    public AjaxResult<Void> deleteByIds(List<Long> ids) {
        return internalAdminItemClient.deleteByIds(ids);
    }

    public AjaxResult<Void> add(AdminItemDTO itemDTO) {
        return internalAdminItemClient.add(itemDTO);
    }

    public AjaxResult<Void> update(AdminItemDTO itemDTO) {
        return internalAdminItemClient.update(itemDTO);
    }

    public AjaxResult<Object> dashboard() {
        return internalAdminItemClient.dashboard();
    }

    public void export(HttpServletResponse response) {
        AjaxResult<List<AdminItemExportVO>> result = internalAdminItemClient.exportData();
        ExcelUtils.exportEasyExcel(response, result.getData(), AdminItemExportVO.class, "items");
    }
}
