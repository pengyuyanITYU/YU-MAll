# yu-api

跨服务通信契约模块，Feign 客户端 + DTO/VO 集中定义，避免服务间循环依赖。

## STRUCTURE

```
com.yu.api/
├── client/          # Feign 客户端接口
├── dto/             # 数据传输对象
├── vo/              # 视图对象
├── po/              # 共享实体
├── query/           # 查询参数
├── fallbacks/       # FallbackFactory 实现
└── enums/           # 共享枚举 (OrderStatus, PayType)
```

## FEIGN CLIENT PATTERN

```java
@FeignClient(value = "yu-mall-xxx-service", path = "/xxx",
             fallbackFactory = XxxFallbackFactory.class)
public interface XxxClient {
    @GetMapping("/{id}") AjaxResult<XxxVO> getXxxById(@PathVariable("id") Long id);
}
```

## NAMING

| 类型 | 命名 | 示例 |
|-----|------|------|
| Client | `XxxClient` | `ItemClient`, `OrderClient` |
| DTO | `XxxDTO` | `ItemDTO`, `PayOrderDTO` |
| VO | `XxxVO` | `ItemDetailVO` |
| Fallback | `XxxFallbackFactory` | `ItemFallbackFactory` |

## WORKFLOW

修改后必须执行：
```bash
mvn -pl yu-api clean install -DskipTests
```

**新增 Client:** 1) 创建 `XxxClient.java` 2) 创建 `XxxFallbackFactory.java` 3) 添加 DTO/VO 4) mvn install

## 注意

- 库模块，无 main()，不可独立运行
- 所有客户端返回 `AjaxResult<T>` 包装
- 使用 `@PathVariable("name")` 显式指定参数名
