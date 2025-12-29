package com.yu.api.client;


import com.yu.api.dto.PayOrderDTO;
import com.yu.api.vo.SaleDashboardVO;
import com.yu.common.domain.AjaxResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "yu-mall-pay-service", path = "admin/sales")
public interface PayClient {

    @GetMapping("/dashboard")
     AjaxResult<SaleDashboardVO> getSaleDashboard();
}
