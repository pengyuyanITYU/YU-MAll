package com.yu.admin.service;

import com.yu.api.client.InternalAdminOrderClient;
import com.yu.api.query.admin.AdminOrderPageQuery;
import com.yu.api.vo.admin.AdminOrderExportVO;
import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.common.utils.ExcelUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final InternalAdminOrderClient internalAdminOrderClient;

    public TableDataInfo list(AdminOrderPageQuery query) {
        return internalAdminOrderClient.list(query);
    }

    public AjaxResult<Object> getById(Long id) {
        return internalAdminOrderClient.getById(id);
    }

    public AjaxResult<Void> delete(Long id) {
        return internalAdminOrderClient.delete(id);
    }

    public AjaxResult<Void> deleteByIds(List<Long> ids) {
        return internalAdminOrderClient.deleteByIds(ids);
    }

    public AjaxResult<Object> recent() {
        return internalAdminOrderClient.recent();
    }

    public void export(HttpServletResponse response) {
        AjaxResult<List<AdminOrderExportVO>> result = internalAdminOrderClient.exportData();
        ExcelUtils.exportEasyExcel(response, result.getData(), AdminOrderExportVO.class, "orders");
    }
}
