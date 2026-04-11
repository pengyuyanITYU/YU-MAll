package com.yu.admin.service;

import com.yu.api.client.InternalAdminShopClient;
import com.yu.api.dto.admin.AdminShopDTO;
import com.yu.api.query.admin.AdminShopPageQuery;
import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminShopService {

    private final InternalAdminShopClient internalAdminShopClient;

    public TableDataInfo list(AdminShopPageQuery query) {
        return internalAdminShopClient.list(query);
    }

    public AjaxResult<Object> simpleList() {
        return internalAdminShopClient.simpleList();
    }

    public AjaxResult<Object> getById(Long id) {
        return internalAdminShopClient.getById(id);
    }

    public AjaxResult<Void> add(AdminShopDTO shopDTO) {
        return internalAdminShopClient.add(shopDTO);
    }

    public AjaxResult<Void> update(AdminShopDTO shopDTO) {
        return internalAdminShopClient.update(shopDTO);
    }

    public AjaxResult<Void> delete(Long id) {
        return internalAdminShopClient.delete(id);
    }
}
