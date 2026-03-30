# Repository Guidelines

## Project Overview

鱼的电商系统 (YU-MALL) - 微服务架构电商平台，包含用户、商品、订单、支付、购物车等核心模块。

## Project Structure & Module Organization

```
yu-mall/
├── pom.xml                    # 父POM，管理依赖版本
├── yu-common/                 # 公共模块：工具类、异常、通用响应
├── yu-api/                    # Feign客户端、DTO、VO（服务间通信）
├── yu-gateway/                # API网关
├── user-service/              # 用户服务
├── item-service/              # 商品服务
├── order-service/             # 订单服务
├── pay-service/               # 支付服务
├── cart-service/              # 购物车服务
├── address-service/           # 地址服务
├── member-service/            # 会员服务
├── collect-service/           # 收藏服务
├── comment-service/           # 评论服务
├── frontend/                  # 用户端前端 (Vue 3)
└── frontend-admin/            # 管理端前端 (Vue 3)
```

**模块说明：**
- `yu-common`：公共配置、工具类、异常定义、通用响应实体（不可独立运行）
- `yu-api`：Feign客户端接口、跨服务DTO/VO（作为依赖被其他服务引入，不可独立运行）
- 业务服务模块：标准 Spring Boot 应用，可独立部署

## Build, Test, and Development Commands

### Backend (Java 11 Required)

```bash
# 构建所有模块（跳过测试）
mvn clean package -DskipTests

# 构建并运行测试
mvn clean test

# 构建单个模块
mvn -pl item-service clean package -DskipTests

# 运行单个服务的测试
mvn -pl item-service test

# 运行单个测试类
mvn -pl item-service test -Dtest=ItemServiceImplTest

# 运行单个测试方法
mvn -pl item-service test -Dtest=ItemServiceImplTest#getItemById_ItemFound_ReturnCompleteVO

# 启动单个服务（本地开发）
mvn -pl user-service spring-boot:run

# 刷新 yu-api 本地依赖（修改 API 模块后执行）
mvn -pl yu-api clean install -DskipTests
```

### Frontend

```bash
# 用户端
cd frontend && npm install && npm run dev

# 管理端
cd frontend-admin && npm install && npm run dev

# 构建生产版本
npm run build

# 代码检查（仅 frontend 有 lint）
npm run lint
```

## Technology Stack

### Backend
- **Framework**: Spring Boot 2.7.12 + Spring Cloud 2021.0.3 + Spring Cloud Alibaba 2021.0.4.0
- **ORM**: MyBatis-Plus 3.5.3.1
- **Database**: MySQL 8.0.23 + Druid 连接池
- **Cache**: Redis + Caffeine
- **Message Queue**: RabbitMQ
- **Registry/Config**: Nacos (默认: 192.168.100.128:8848)
- **Distributed Transaction**: Seata
- **API Doc**: Knife4j 3.0.3 (Swagger)
- **Tools**: Lombok, Hutool 5.8.11, EasyExcel

### Frontend
- **Framework**: Vue 3 + TypeScript + Vite
- **UI**: Element Plus + Naive UI
- **State**: Pinia (with persistedstate)
- **HTTP**: Axios
- **Style**: SCSS + Tailwind CSS

## Coding Style & Naming Conventions

### Java Code Style

**基础规范：**
- 缩进：4空格
- 编码：UTF-8
- JDK：11

**包命名：** `com.yu.<module>...`

```
com.yu.item.controller      # 控制器
com.yu.item.service         # 服务接口
com.yu.item.service.impl    # 服务实现
com.yu.item.mapper          # MyBatis Mapper
com.yu.item.domain.po       # 数据库实体
com.yu.item.domain.dto      # 数据传输对象
com.yu.item.domain.vo       # 视图对象
com.yu.item.domain.query    # 查询参数对象
```

**类命名模式：**

| 类型 | 命名规则 | 示例 |
|------|----------|------|
| Controller | `*Controller` | `ItemController`, `AdminItemController` |
| Service接口 | `I*Service` | `IItemService` |
| Service实现 | `*ServiceImpl` | `ItemServiceImpl` |
| Mapper | `*Mapper` | `ItemMapper` |
| 实体 | `*` (表名驼峰) | `Item`, `OrderDetail` |
| DTO | `*DTO` | `ItemDTO`, `OrderFormDTO` |
| VO | `*VO` | `ItemDetailVO`, `UserVO` |
| Query | `*Query` / `*PageQuery` | `ItemPageQuery` |

**常用注解：**
```java
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor  // 构造器注入
@Slf4j                   // 日志
public class ItemController {
    private final IItemService itemService;
}
```

**Controller 返回类型：**
- 单对象：`AjaxResult<T>` → `AjaxResult.success(data)`
- 分页列表：`TableDataInfo` (包含 rows, total, code, msg)
- 操作结果：`AjaxResult.toAjax(rows)` 或 `AjaxResult.success()`

**Service 层规范：**
```java
// 接口继承 IService<Entity>
public interface IItemService extends IService<Item> {
    ItemDetailVO getItemById(Long id);
}

// 实现继承 ServiceImpl<Mapper, Entity>
@Service
@RequiredArgsConstructor
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements IItemService {
    private final ItemMapper itemMapper;
    // 使用 lambdaQuery() / lambdaUpdate() 进行链式操作
}
```

**异常处理：**
```java
// 业务异常
throw new BusinessException("商品不存在");
// 参数异常
throw new BadRequestException("参数错误");
// 数据库异常
throw new DbException("数据库操作失败");
```

**分布式事务：**
```java
@GlobalTransactional(rollbackFor = Exception.class)
public void add(ItemDTO itemDTO) { ... }
```

### Frontend Code Style

**目录结构：**
```
frontend/src/
├── api/           # API 请求封装
├── components/    # 公共组件
├── views/         # 页面组件
├── router/        # 路由配置
├── stores/        # Pinia 状态
└── utils/         # 工具函数
```

**Vue 组件风格：**
- 使用 `<script setup lang="ts">`
- 使用 Composition API (ref, reactive, onMounted)
- 样式使用 `<style scoped lang="scss">`

**API 调用：**
```typescript
import { list } from '@/api/item';
const res = await list(params);
if (res.code === 200) { ... }
```

## Testing Guidelines

**测试框架：** Spring Boot Test + JUnit 5 + Mockito

**测试命名：** `*Test.java`，位于 `src/test/java`

**测试示例：**
```java
class ItemServiceImplTest {
    @Mock private ItemMapper itemMapper;
    @InjectMocks private ItemServiceImpl itemService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getItemById_ItemNotFound_ReturnNull() {
        when(itemMapper.selectById(1L)).thenReturn(null);
        assertNull(itemService.getItemById(1L));
    }
}
```

**运行测试：**
```bash
# 运行所有测试
mvn test

# 运行指定模块测试
mvn -pl item-service test

# 运行单个测试类
mvn -pl item-service test -Dtest=ItemServiceImplTest
```

## Commit & Pull Request Guidelines

**提交信息格式：** `<module>: <description>`

```
item-service: add SKU stock validation
order-service: fix timeout consumer retry
yu-api: add PayOrderDTO fields
frontend: update item detail page layout
```

**PR 检查清单：**
- [ ] 变更模块列表
- [ ] 配置变更说明（特别是 Nacos）
- [ ] 测试命令及结果
- [ ] 前端 UI 变更截图

## Security & Configuration

- **不要提交密钥到 Git**（数据库密码、Nacos 配置等）
- **本地环境**：Nacos 默认 `192.168.100.128:8848`
- **yu-api 是库模块**：不打包为可执行 JAR，需 `mvn install` 后其他服务才能引用
- **跨服务调用**：通过 Feign Client (`yu-api/client/*.java`)

## Common Patterns

### 新增 CRUD 接口

1. 定义 PO (`domain/po/Xxx.java`)
2. 定义 DTO/VO (`domain/dto/XxxDTO.java`, `domain/vo/XxxVO.java`)
3. 创建 Mapper (`mapper/XxxMapper.java extends BaseMapper<Xxx>`)
4. 创建 Service 接口 (`service/IXxxService extends IService<Xxx>`)
5. 实现 Service (`service/impl/XxxServiceImpl`)
6. 创建 Controller (`controller/XxxController`)

### 分页查询

```java
// Controller
@GetMapping("/list")
public TableDataInfo list(ItemPageQuery query) {
    return itemService.listItem(query);
}

// Service
public TableDataInfo listItem(ItemPageQuery query) {
    Page<Item> page = new Page<>(query.getPageNo(), query.getPageSize());
    Page<Item> result = lambdaQuery()
        .eq(Item::getStatus, 1)
        .page(page);
    TableDataInfo info = new TableDataInfo();
    info.setRows(result.getRecords());
    info.setTotal(result.getTotal());
    return info;
}
```

### Feign 跨服务调用

```java
// yu-api/client/ItemClient.java
@FeignClient(value = "yu-mall-item-service", path = "/items", 
             fallbackFactory = ItemFallbackFactory.class)
public interface ItemClient {
    @GetMapping("/{id}")
    AjaxResult<ItemDetailVO> getItemById(@PathVariable("id") Long id);
}

// 调用方服务
@Autowired
private ItemClient itemClient;

public void someMethod() {
    AjaxResult<ItemDetailVO> result = itemClient.getItemById(1L);
}
```
