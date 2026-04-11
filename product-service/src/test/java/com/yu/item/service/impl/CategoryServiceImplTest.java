package com.yu.item.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.yu.item.domain.dto.CategoryDTO;
import com.yu.item.domain.po.Category;
import com.yu.item.domain.po.Item;
import com.yu.item.service.IItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private IItemService itemService;

    @Mock(answer = Answers.RETURNS_SELF)
    private LambdaQueryChainWrapper<Category> categoryQueryWrapper;

    @Mock(answer = Answers.RETURNS_SELF)
    private LambdaQueryChainWrapper<Item> itemQueryWrapper;

    @Mock(answer = Answers.RETURNS_SELF)
    private LambdaUpdateChainWrapper<Item> itemUpdateWrapper;

    private CategoryServiceImpl categoryService;

    @BeforeEach
    void setUp() {
        categoryService = spy(new CategoryServiceImpl(itemService));
    }

    @Test
    void updateCategoryShouldSyncBoundItemCategoryNameWhenNameChanges() {
        Category category = new Category();
        category.setId(1L);
        category.setName("old-category");

        doReturn(categoryQueryWrapper).when(categoryService).lambdaQuery();
        doReturn(category).when(categoryService).getById(1L);
        doReturn(true).when(categoryService).updateById(any(Category.class));
        when(categoryQueryWrapper.eq(any(), eq("new-category"))).thenReturn(categoryQueryWrapper);
        when(categoryQueryWrapper.ne(any(), eq(1L))).thenReturn(categoryQueryWrapper);
        when(categoryQueryWrapper.count()).thenReturn(0L);

        when(itemService.lambdaQuery()).thenReturn(itemQueryWrapper);
        when(itemQueryWrapper.eq(any(), eq(1L))).thenReturn(itemQueryWrapper);
        when(itemQueryWrapper.count()).thenReturn(2L);

        when(itemService.lambdaUpdate()).thenReturn(itemUpdateWrapper);
        when(itemUpdateWrapper.eq(any(), eq(1L))).thenReturn(itemUpdateWrapper);
        when(itemUpdateWrapper.set(any(), eq("new-category"))).thenReturn(itemUpdateWrapper);
        when(itemUpdateWrapper.update()).thenReturn(true);

        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setId(1L);
        categoryDTO.setName("new-category");

        boolean updated = categoryService.updateCategory(categoryDTO);

        assertTrue(updated);
        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryService).updateById(categoryCaptor.capture());
        assertEquals("new-category", categoryCaptor.getValue().getName());
        verify(itemUpdateWrapper).eq(any(), eq(1L));
        verify(itemUpdateWrapper).set(any(), eq("new-category"));
        verify(itemUpdateWrapper).update();
    }
}
