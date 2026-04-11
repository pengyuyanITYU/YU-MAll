package com.yu.item.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yu.common.domain.page.TableDataInfo;
import com.yu.common.exception.BizIllegalException;
import com.yu.item.domain.dto.CategoryDTO;
import com.yu.item.domain.po.Category;
import com.yu.item.domain.po.Item;
import com.yu.item.domain.query.CategoryQuery;
import com.yu.item.domain.vo.CategoryVO;
import com.yu.item.mapper.CategoryMapper;
import com.yu.item.service.ICategoryService;
import com.yu.item.service.IItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

    private final @Lazy IItemService itemService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addCategory(CategoryDTO categoryDTO) {
        if (categoryDTO == null || StrUtil.isBlank(categoryDTO.getName())) {
            throw new BizIllegalException("分类名称不能为空");
        }
        Long count = lambdaQuery().eq(Category::getName, categoryDTO.getName()).count();
        if (count != null && count > 0) {
            throw new BizIllegalException("分类名称已存在");
        }
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        return save(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCategory(Long id) {
        if (id == null || id <= 0) {
            throw new BizIllegalException("分类ID不合法");
        }
        Category category = getById(id);
        if (category == null) {
            throw new BizIllegalException("分类不存在");
        }
        if (itemService.lambdaQuery().eq(Item::getCategoryId, id).count() > 0) {
            throw new BizIllegalException("分类下存在商品，无法删除");
        }
        return removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCategory(CategoryDTO categoryDTO) {
        if (categoryDTO == null || categoryDTO.getId() == null || categoryDTO.getId() <= 0) {
            throw new BizIllegalException("分类ID不合法");
        }
        if (StrUtil.isBlank(categoryDTO.getName())) {
            throw new BizIllegalException("分类名称不能为空");
        }
        Category category = getById(categoryDTO.getId());
        if (category == null) {
            throw new BizIllegalException("分类不存在");
        }
        Long count = lambdaQuery()
                .eq(Category::getName, categoryDTO.getName())
                .ne(Category::getId, categoryDTO.getId())
                .count();
        if (count != null && count > 0) {
            throw new BizIllegalException("分类名称已存在");
        }
        boolean categoryNameChanged = !StrUtil.equals(category.getName(), categoryDTO.getName());
        BeanUtils.copyProperties(categoryDTO, category);
        boolean updated = updateById(category);
        if (!updated) {
            return false;
        }
        if (!categoryNameChanged) {
            return true;
        }
        Long itemCount = itemService.lambdaQuery()
                .eq(Item::getCategoryId, categoryDTO.getId())
                .count();
        if (itemCount == null || itemCount <= 0) {
            return true;
        }
        return itemService.lambdaUpdate()
                .eq(Item::getCategoryId, categoryDTO.getId())
                .set(Item::getCategory, categoryDTO.getName())
                .update();
    }

    @Override
    public CategoryVO getCategoryById(Long id) {
        if (id == null || id <= 0) {
            throw new BizIllegalException("分类ID不合法");
        }
        Category category = getById(id);
        if (category == null) {
            throw new BizIllegalException("分类不存在");
        }
        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(category, vo);
        return vo;
    }

    @Override
    public TableDataInfo getAllCategories(CategoryQuery query) {
        CategoryQuery realQuery = query == null ? new CategoryQuery() : query;
        Page<Category> page = new Page<>(realQuery.getPageNo(), realQuery.getPageSize());
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(realQuery.getName()), Category::getName, realQuery.getName());
        Page<Category> result = page(page, wrapper);
        return TableDataInfo.success(result.getRecords(), result.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            throw new BizIllegalException("分类ID不能为空");
        }
        for (Long id : ids) {
            if (id == null || id <= 0) {
                throw new BizIllegalException("分类ID不合法");
            }
            if (itemService.lambdaQuery().eq(Item::getCategoryId, id).count() > 0) {
                throw new BizIllegalException("分类下存在商品，无法删除");
            }
        }
        return removeByIds(ids);
    }
}
