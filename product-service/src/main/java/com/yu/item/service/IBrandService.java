package com.yu.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.item.domain.dto.BrandDTO;
import com.yu.item.domain.po.Brand;
import com.yu.item.domain.query.BrandQuery;
import com.yu.item.domain.vo.BrandVO;

import java.util.List;

public interface IBrandService extends IService<Brand> {

    boolean addBrand(BrandDTO brandDTO);

    boolean deleteBrand(Long id);

    boolean updateBrand(BrandDTO brandDTO);

    BrandVO getBrandById(Long id);

    TableDataInfo getAllBrands(BrandQuery query);

    boolean deleteByIds(List<Long> ids);
}
