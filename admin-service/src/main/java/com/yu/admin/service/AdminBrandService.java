package com.yu.admin.service;

import com.yu.api.client.InternalAdminBrandClient;
import com.yu.api.dto.admin.AdminBrandDTO;
import com.yu.api.query.admin.AdminBrandPageQuery;
import com.yu.api.vo.admin.AdminBrandExportVO;
import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.common.utils.ExcelUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminBrandService {

    private final InternalAdminBrandClient internalAdminBrandClient;

    public AjaxResult<Void> add(AdminBrandDTO brandDTO) {
        return internalAdminBrandClient.add(brandDTO);
    }

    public AjaxResult<Void> delete(Long id) {
        return internalAdminBrandClient.delete(id);
    }

    public AjaxResult<Void> deleteByIds(List<Long> ids) {
        return internalAdminBrandClient.deleteByIds(ids);
    }

    public AjaxResult<Void> update(AdminBrandDTO brandDTO) {
        return internalAdminBrandClient.update(brandDTO);
    }

    public AjaxResult<Object> getById(Long id) {
        return internalAdminBrandClient.getById(id);
    }

    public TableDataInfo list(AdminBrandPageQuery query) {
        return internalAdminBrandClient.list(query);
    }

    public AjaxResult<Object> simpleList() {
        return internalAdminBrandClient.simpleList();
    }

    public void export(HttpServletResponse response) {
        AjaxResult<List<AdminBrandExportVO>> result = internalAdminBrandClient.exportData();
        ExcelUtils.exportEasyExcel(response, result.getData(), AdminBrandExportVO.class, "brands");
    }
}
