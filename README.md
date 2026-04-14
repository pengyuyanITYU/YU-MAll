# YU-MALL

鱼的电商系统，当前仓库是基于 Spring Cloud Alibaba 的微服务电商项目，包含用户端、管理端和多个业务服务。

## 当前版本

- Java 21
- Spring Boot 3.5.13
- Spring Cloud 2025.0.1
- Spring Cloud Alibaba 2025.0.0.0
- MyBatis-Plus 3.5.15
- Vue 3 + TypeScript + Vite

## 模块结构

后端模块：
- `admin-service`
- `user-center-service`
- `product-service`
- `order-service`
- `pay-service`
- `ai-service`
- `yu-gateway`
- `yu-common`
- `yu-api`

前端模块：
- `frontend`
- `frontend-admin`

## 用户认证防刷

当前用户端登录和注册链路已经补充以下能力：
- 滑块验证码：新增 `POST /users/captcha/slider/challenge` 和 `POST /users/captcha/slider/verify`
- 一次性票据：滑块校验成功后返回 `captchaTicket`，注册强制消费，登录在风险场景下强制消费
- Redis 令牌桶：对挑战、校验、登录、注册分别做全局和维度限流
- Redis 分布式锁：注册保存前对 `username + phone` 组合加锁，降低并发抢注
- Sentinel 总闸门：`captcha-challenge`、`captcha-verify`、`user-login`、`user-register` 四个资源做接口级限流

固定规则：
- 滑块挑战 TTL：120 秒
- 滑块票据 TTL：180 秒
- 注册没有有效票据直接返回 `428`
- 高频请求命中限流返回 `429`
- 登录连续失败达到阈值后会要求先过滑块再继续登录

## 本地联调依赖

本地至少需要准备以下基础设施：
- MySQL
- Redis
- Nacos
- RabbitMQ

当前配置默认读取的 Nacos 地址是 `192.168.100.128:8848`，Redis 默认读取 `192.168.100.128:6379`。如果你的本地环境不同，需要先按实际环境调整对应配置。

## 启动说明

后端常用：

```powershell
# Java 21
$env:JAVA_HOME = "C:\Users\20817\.jdks\corretto-21.0.9"
$env:PATH = "$env:JAVA_HOME\bin;" + [System.Environment]::GetEnvironmentVariable("PATH","Machine")

# 公共模块改动后建议先安装
mvn -pl yu-common,yu-api -am install -DskipTests

# 启动用户中心
mvn -pl user-center-service spring-boot:run

# 启动网关
mvn -pl yu-gateway spring-boot:run
```

前端常用：

```powershell
cd frontend
npm install
npm run dev
```

## 本次相关接口

- `POST /users/login`
- `POST /users/register`
- `POST /users/captcha/slider/challenge`
- `POST /users/captcha/slider/verify`

登录流程：
- 默认先直接登录
- 服务端返回 `428` 时，前端弹出滑块
- 滑块通过后自动携带 `captchaTicket` 重试一次

注册流程：
- 点击注册时先完成滑块验证
- 成功拿到 `captchaTicket` 后再提交注册

## 验证命令

后端定向测试：

```powershell
mvn -pl yu-common,user-center-service,yu-gateway "-Dtest=WebUtilsClientIpTest,AuthRiskPolicyTest,UserCaptchaServiceImplTest,MyGlobalFilterTest" test
```

前端定向检查：

```powershell
node --test .\frontend\tests\auth-captcha.test.mjs
cd frontend
npm run build
```
