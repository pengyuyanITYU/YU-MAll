package com.yu.admin.service;

import com.yu.api.client.InternalAdminItemClient;
import com.yu.api.dto.admin.AdminItemDTO;
import com.yu.api.query.admin.AdminItemPageQuery;
import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminItemServiceTest {

    @Mock
    private InternalAdminItemClient internalAdminItemClient;

    @InjectMocks
    private AdminItemService adminItemService;

    @Test
    void listShouldDelegateToInternalProductAdminClient() {
        AdminItemPageQuery query = new AdminItemPageQuery();
        TableDataInfo expected = new TableDataInfo();
        when(internalAdminItemClient.list(query)).thenReturn(expected);

        TableDataInfo actual = adminItemService.list(query);

        assertSame(expected, actual);
        verify(internalAdminItemClient).list(query);
    }

    @Test
    void addShouldDelegateToInternalProductAdminClient() {
        AdminItemDTO itemDTO = new AdminItemDTO();
        AjaxResult<Void> expected = AjaxResult.success();
        when(internalAdminItemClient.add(itemDTO)).thenReturn(expected);

        AjaxResult<Void> actual = adminItemService.add(itemDTO);

        assertSame(expected, actual);
        verify(internalAdminItemClient).add(itemDTO);
    }
}
