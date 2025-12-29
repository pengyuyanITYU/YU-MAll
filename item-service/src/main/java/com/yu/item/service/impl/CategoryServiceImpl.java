package com.yu.item.service.impl;

import cn.hutool.core.collection.CollUtil;
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
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;


@Service
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

    @Autowired
    @Lazy
    private IItemService itemService;



    @Override
    public boolean addCategory(CategoryDTO categoryDTO) {
        // 参数校验
        if (!StringUtils.hasText(categoryDTO.getName())) {
            throw new BizIllegalException("分类名称不能为空");
        }

        // 检查分类名称是否已存在
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getName, categoryDTO.getName());
        Category existCategory = this.getOne(queryWrapper);
        if (existCategory != null) {
            throw new BizIllegalException("分类名称已存在");
        }

        // 创建分类实体
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);

        return this.save(category);
    }

    @Override
    public boolean deleteCategory(Long id) {
        // 参数校验
        if (id == null) {
            throw new BizIllegalException("分类ID不能为空");
        }
        if (id <= 0) {
            throw new BizIllegalException("分类ID不能小于等于0");
        }

        // 检查分类是否存在
        Category category = this.getById(id);
        if (category == null) {
            throw new BizIllegalException("分类不存在");
        }
        if (itemService.lambdaQuery().eq(Item::getCategoryId, id).count() > 0) {
            throw new BizIllegalException("该分类下存在商品，无法删除");
        }

        return this.removeById(id);
    }

    @Override
    public boolean deleteByIds(List<Long> ids) {
        if (CollUtil.isNotEmpty(ids)) {
            throw new BizIllegalException("分类ID不能为空");
        }
        ids.stream().forEach(
                id -> {
                    if (itemService.lambdaQuery().eq(Item::getCategoryId, id).count() > 0) {
                        throw new BizIllegalException("该分类下存在商品，无法删除");
                    }
                }
        );
        return removeByIds(ids);
    }

    @Override
    public boolean updateCategory(CategoryDTO categoryDTO) {
        // 参数校验
        if (categoryDTO.getId() == null) {
            throw new BizIllegalException("分类ID不能为空");
        }
        if (!StringUtils.hasText(categoryDTO.getName())) {
            throw new BizIllegalException("分类名称不能为空");
        }

        // 检查分类是否存在
        Category category = this.getById(categoryDTO.getId());
        if (category == null) {
            throw new BizIllegalException("分类不存在");
        }

        // 检查分类名称是否已被其他分类使用
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getName, categoryDTO.getName())
                .ne(Category::getId, categoryDTO.getId());
        Category existCategory = this.getOne(queryWrapper);
        if (existCategory != null) {
            throw new BizIllegalException("分类名称已存在");
        }

        // 更新分类信息
        BeanUtils.copyProperties(categoryDTO, category);

        return this.updateById(category);
    }

    @Override
    public CategoryVO getCategoryById(Long id) {
        // 参数校验
        if (id == null) {
            throw new BizIllegalException("分类ID不能为空");
        }
        

        // 查询分类信息
        Category category = this.getById(id);
        itemService.lambdaQuery().eq(Item::getCategoryId, id);
        if (category == null) {
            throw new BizIllegalException("分类不存在");
        }

        // 转换为VO
        CategoryVO categoryVO = new CategoryVO();
        BeanUtils.copyProperties(category, categoryVO);

        return categoryVO;
    }

    @Override
    public TableDataInfo getAllCategories(CategoryQuery query) {
        Page<Category> categoryPage = new Page<>(query.getPageNo(), query.getPageSize());
        Page<Category> page = lambdaQuery().like(Category::getName, query.getName())
                .page(categoryPage);
        TableDataInfo success = TableDataInfo.success(page.getRecords(), page.getTotal());
        return success;
    }
}