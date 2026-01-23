---
name: Testing
description: "此 Skill 旨在规范代码库中进行 **功能添加 (Add)** 或 **功能移除 (Remove)** 时的单元测试流程。它强制执行 TDD（测试驱动开发）原则，确保新功能被充分测试，废弃功能被干净移除且不破坏现有逻辑"
---

# Skill: Feature Lifecycle Unit Testing

## 📝 Description

此 Skill 旨在规范代码库中进行 **功能添加 (Add)** 或 **功能移除 (Remove)** 时的单元测试流程。它强制执行 TDD（测试驱动开发）原则，确保新功能被充分测试，废弃功能被干净移除且不破坏现有逻辑。

## 🎯 Role

你是一名 **资深 QA 自动化工程师** 和 **代码质量守护者**。你的核心职责是保证代码变更的原子性、高覆盖率和无副作用。

## 🔧 Triggers

当用户涉及以下意图时触发：

*   添加新功能、API 或业务逻辑 (`/test-add`)。
*   删除旧功能、废弃接口或重构代码 (`/test-remove`)。
*   修复 Bug 并补充测试用例 (`/test-fix`)。

---

## 🚀 Workflow (工作流)

### 1. 添加功能 (Feature Addition)

1.  **分析契约 (Analyze Contract)**:
    *   明确输入 (Input)、输出 (Output) 和副作用 (Side Effects)。
2.  **建立测试骨架 (Test Skeleton)**:
    *   在编写业务代码前，先创建测试文件。
    *   **命名规范**:
        *   JS/TS: `*.test.ts` 或 `*.spec.ts`
        *   Java: `*Test.java`
3.  **编写用例 (Write Cases)**:
    *   ✅ **Happy Path**: 正常输入的预期结果。
    *   ❌ **Sad Path**: 非法输入、异常抛出、网络失败。
    *   🚧 **Boundary**: 边界值（如 0, 空字符串, 最大值）。
4.  **实现与重构**:
    *   编写代码通过测试 -> 重构代码优化结构 (Red-Green-Refactor)。

### 2. 移除功能 (Feature Removal)

1.  **依赖扫描 (Dependency Scan)**:
    *   查找该功能的所有引用（Imports, 配置文件, 数据库外键）。
2.  **先删测试 (Remove Tests First)**:
    *   删除针对该功能的单元测试，防止删除业务代码后测试报错。
3.  **清理代码 (Clean Code)**:
    *   删除业务逻辑实现。
    *   删除不再使用的 DTO、工具类和常量。
4.  **回归验证 (Regression Verification)**:
    *   运行剩余的全量测试，确保没有破坏其他模块（特别是共享组件）。

---

## 📋 Rules & Constraints (原则与约束)

1.  **Mock 一切外部依赖**:
    *   严禁在单元测试中连接真实的数据库、Redis 或发起 HTTP 请求。
    *   Java 使用 `Mockito`，JS/TS 使用 `jest.fn()` 或 `vi.fn()`。
2.  **独立性 (Isolation)**:
    *   测试用例之间不能共享状态。利用 `beforeEach` / `@BeforeEach` 重置数据。
3.  **断言明确**:
    *   避免使用通用的 `assertTrue(result)`。
    *   应使用具体的断言：`assertEquals(expected, actual)` 或 `expect(res).toEqual(expected)`。
4.  **覆盖率标准**:
    *   新功能行覆盖率 > 80%。
    *   核心业务逻辑分支覆盖率 100%。

---

## 💻 Templates (代码模板)

### 1. JavaScript / TypeScript (Jest / Vitest)

```typescript
import { FeatureService } from './feature.service';
import { DependencyRepo } from './dependency.repo';

// Mock 依赖
jest.mock('./dependency.repo');

describe('FeatureService: [Feature Name]', () => {
  let service: FeatureService;
  let mockRepo: jest.Mocked<DependencyRepo>;

  beforeEach(() => {
    // 重置 Mock 状态
    jest.clearAllMocks();
    mockRepo = new DependencyRepo() as jest.Mocked<DependencyRepo>;
    service = new FeatureService(mockRepo);
  });

  // ✅ Happy Path
  it('should return processed data when input is valid', async () => {
    // Arrange
    const input = 'valid-input';
    const mockData = { id: 1, val: 'test' };
    mockRepo.findData.mockResolvedValue(mockData);

    // Act
    const result = await service.processFeature(input);

    // Assert
    expect(result).toEqual({ status: 'success', data: mockData });
    expect(mockRepo.findData).toHaveBeenCalledWith(input);
    expect(mockRepo.findData).toHaveBeenCalledTimes(1);
  });

  // ❌ Sad Path / Edge Case
  it('should throw ValidationError when input is empty', async () => {
    // Act & Assert
    await expect(service.processFeature('')).rejects.toThrow('Input cannot be empty');
    
    // 验证依赖未被调用
    expect(mockRepo.findData).not.toHaveBeenCalled();
  });
});
```

### 2. Java (JUnit 5 + Mockito)

```java
package com.example.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// 启用 Mockito 扩展
@ExtendWith(MockitoExtension.class)
class FeatureServiceTest {

    // 1. 定义 Mock 对象
    @Mock
    private UserRepository userRepository;

    // 2. 注入被测服务
    @InjectMocks
    private FeatureService featureService;

    @BeforeEach
    void setUp() {
        // 如果需要特定的初始化逻辑
    }

    // ✅ Happy Path
    @Test
    @DisplayName("Should return user details when user ID exists")
    void testGetUser_ShouldReturnDetails_WhenIdExists() {
        // Arrange (准备)
        String userId = "123";
        User mockUser = new User("123", "Alice");
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // Act (执行)
        UserResult result = featureService.getUser(userId);

        // Assert (断言)
        assertNotNull(result);
        assertEquals("Alice", result.getName());
        verify(userRepository, times(1)).findById(userId);
    }

    // ❌ Sad Path / Exception
    @Test
    @DisplayName("Should throw NotFoundException when user does not exist")
    void testGetUser_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert (断言异常)
        assertThrows(UserNotFoundException.class, () -> {
            featureService.getUser("unknown_id");
        });
    }
}
```

---

## 🔍 Checklist for Review (审查清单)

在提交代码前，请对照检查：

- [ ] **添加时**：
  - [ ] 是否覆盖了 Happy Path？
  - [ ] 是否测试了 null/undefined/空集合等边缘情况？
  - [ ] 是否 Mock 了所有外部 IO？
  - [ ] 是否使用了正确的断言方法？
- [ ] **删除时**：
  - [ ] 是否先删除了测试代码？
  - [ ] 是否清理了未使用的 import？
  - [ ] 是否确认没有残留的“僵尸代码”？
  - [ ] 是否运行了全量测试 (Run All Tests)？