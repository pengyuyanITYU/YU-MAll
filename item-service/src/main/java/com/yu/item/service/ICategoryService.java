package com.yu.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.item.domain.dto.CategoryDTO;
import com.yu.item.domain.po.Category;
import com.yu.item.domain.query.CategoryQuery;
import com.yu.item.domain.vo.CategoryVO;

import java.util.List;

public interface ICategoryService extends IService<Category> {

    boolean addCategory(CategoryDTO categoryDTO);

    boolean deleteCategory(Long id);

    boolean updateCategory(CategoryDTO categoryDTO);

    CategoryVO getCategoryById(Long id);

    TableDataInfo getAllCategories(CategoryQuery query);

    boolean deleteByIds(List<Long> ids);
}
