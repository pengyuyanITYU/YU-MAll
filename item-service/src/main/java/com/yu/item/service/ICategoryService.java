package com.yu.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.item.domain.dto.CategoryDTO;
import com.yu.item.domain.po.Category;
import com.yu.item.domain.query.CategoryQuery;
import com.yu.item.domain.vo.CategoryVO;


import java.util.List;

/**
 * 商品分类服务接口
 * @author yu
 * @since 2023-12-10
 */
public interface ICategoryService extends IService<Category> {

    /**
     * 新增商品分类
     * @param categoryDTO 分类信息
     * @return 是否成功
     */
    boolean addCategory(CategoryDTO categoryDTO);

    /**
     * 删除商品分类
     * @param id 分类ID
     * @return 是否成功
     */
    boolean deleteCategory(Long id);

    /**
     * 更新商品分类
     * @param categoryDTO 分类信息
     * @return 是否成功
     */
    boolean updateCategory(CategoryDTO categoryDTO);

    /**
     * 根据ID查询商品分类
     * @param id 分类ID
     * @return 分类信息
     */
    CategoryVO getCategoryById(Long id);

    /**
     * 查询所有商品分类
     * @return 分类列表
     */
    TableDataInfo getAllCategories(CategoryQuery query);

    boolean deleteByIds(List<Long> ids);
}