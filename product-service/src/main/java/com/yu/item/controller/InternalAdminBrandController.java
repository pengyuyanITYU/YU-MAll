package com.yu.item.controller;

import com.yu.api.dto.admin.AdminBrandDTO;
import com.yu.api.query.admin.AdminBrandPageQuery;
import com.yu.api.vo.admin.AdminBrandExportVO;
import com.yu.common.domain.AjaxResult;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.item.domain.dto.BrandDTO;
import com.yu.item.domain.po.Brand;
import com.yu.item.domain.query.BrandQuery;
import com.yu.item.service.IBrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/admin/brands")
public class InternalAdminBrandController {

    private final IBrandService brandService;

    @PostMapping
    public AjaxResult<Void> add(@Validated @RequestBody AdminBrandDTO brandDTO) {
        return AjaxResult.toAjax(brandService.addBrand(toBrandDTO(brandDTO)));
    }

    @DeleteMapping("/{id}")
    public AjaxResult<Void> delete(@PathVariable Long id) {
        return AjaxResult.toAjax(brandService.deleteBrand(id));
    }

    @DeleteMapping("/batch")
    public AjaxResult<Void> deleteByIds(@RequestParam("ids") List<Long> ids) {
        return AjaxResult.toAjax(brandService.deleteByIds(ids));
    }

    @PutMapping
    public AjaxResult<Void> update(@Validated @RequestBody AdminBrandDTO brandDTO) {
        return AjaxResult.toAjax(brandService.updateBrand(toBrandDTO(brandDTO)));
    }

    @GetMapping("/{id}")
    public AjaxResult<Object> getById(@PathVariable Long id) {
        return AjaxResult.success(brandService.getBrandById(id));
    }

    @GetMapping
    public TableDataInfo list(AdminBrandPageQuery query) {
        BrandQuery target = new BrandQuery();
        if (query != null) {
            BeanUtils.copyProperties(query, target);
        }
        return brandService.getAllBrands(target);
    }

    @GetMapping("/simple")
    public AjaxResult<Object> simpleList() {
        return AjaxResult.success(brandService.list());
    }

    @GetMapping("/export-data")
    public AjaxResult<List<AdminBrandExportVO>> exportData() {
        return AjaxResult.success(brandService.list().stream().map(this::toExportVO).collect(Collectors.toList()));
    }

    private BrandDTO toBrandDTO(AdminBrandDTO source) {
        BrandDTO target = new BrandDTO();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    private AdminBrandExportVO toExportVO(Brand brand) {
        AdminBrandExportVO exportVO = new AdminBrandExportVO();
        BeanUtils.copyProperties(brand, exportVO);
        return exportVO;
    }
}
