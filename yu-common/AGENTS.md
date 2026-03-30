# yu-common

公共工具模块，作为 Maven 依赖被所有业务服务引入，通过 spring.factories 自动装配。

## STRUCTURE

```
com.yu.common
├── exception/          # 异常体系
├── domain/             # AjaxResult, R, TableDataInfo, PageDTO, PageQuery
├── utils/              # UserContext, BeanUtils, CollUtils, WebUtils
├── config/             # Redis, RabbitMQ, MyBatis, Json, Mvc 自动配置
├── advice/             # CommonExceptionAdvice
├── annotation/         # @AuthCheck, @RateLimit
├── interceptor/        # UserInfoInterceptor
└── ratelimit/          # 限流组件
```

## KEY CLASSES

### 异常体系

```
CommonException (基类)
├── BusinessException      # 业务异常 (500)
├── BadRequestException    # 参数异常 (400)
├── DbException           # 数据库异常 (500)
├── ForbiddenException    # 权限异常 (403)
└── UnauthorizedException # 认证异常 (401)
```

### 响应实体

| 类 | 用途 |
|---|---|
| `AjaxResult<T>` | 通用响应，支持泛型数据 |
| `TableDataInfo` | 分页列表 (rows, total) |
| `PageQuery` | 分页查询基类 (pageNo, pageSize) |

### 工具类

| 类 | 用途 |
|---|---|
| `UserContext` | ThreadLocal 存取当前用户 ID |
| `AdministratorContext` | ThreadLocal 存取管理员 ID |
| `BeanUtils` | Bean 拷贝 (基于 Hutool) |
| `CollUtils` | 集合判空/转换 |

## USAGE PATTERNS

```java
// 异常
throw new BusinessException("商品不存在");
// 响应
return AjaxResult.success(data);
return AjaxResult.toAjax(rows);
// UserContext
Long userId = UserContext.getUser();
UserContext.setUser(userId);  // 拦截器中设置
UserContext.removeUser();     // 请求结束清理
// 注解
@AuthCheck @RateLimit(key="api", time=1, count=5)
```

## AUTO-CONFIGURATION

spring.factories 自动装配：MyMetaObjectHandler, MyBatisConfig, MvcConfig, JsonConfig, RabbitMQConfig。

**注意**: 修改后需 `mvn install` 更新本地仓库，此模块不打包为可执行 JAR。
