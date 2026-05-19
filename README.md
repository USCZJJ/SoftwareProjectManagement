# 软件项目管理系统 PMS

基于 **Spring Boot + Vue 3** 的全栈软件项目管理平台，涵盖项目管理、WBS、需求、任务看板、缺陷跟踪、质量检查和统计可视化等核心功能。

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 2.7.18 |
| 语言 | Java | 17 |
| ORM | MyBatis Plus | 3.5.5 |
| 数据库 | MySQL | 8.x |
| 认证 | JWT (jjwt) | 0.12.5 |
| 接口文档 | Knife4j (OpenAPI 3) | 4.3.0 |
| 工具库 | Hutool | 5.8.25 |
| 前端框架 | Vue 3 | ^3.4.21 |
| UI 库 | Element Plus | ^2.5.6 |
| 状态管理 | Pinia | ^2.1.7 |
| 图表 | ECharts | ^5.5.0 |
| 构建工具 | Vite | ^5.1.4 |

## 功能模块

- **项目管理** — 项目 CRUD、WBS 树、里程碑、任务依赖、关键路径分析（CPM）、计划变更审批、成员管理、甘特图
- **需求管理** — 需求全生命周期（草稿→提交→评审），变更请求，追溯矩阵
- **任务管理** — 看板拖拽（待办/进行中/评审中/完成）、任务分配、进度跟踪、工时日志（每日上限 16h）、任务评审
- **缺陷管理** — 缺陷全状态流转（打开→分配→修复中→已修复→验证中→已验证→关闭→重开），测试用例执行，质量检查清单
- **统计看板** — 项目概览、EVM 指标（PV/EV/AC/SPI/CPI）、缺陷分布统计、工时统计图表

## 项目结构

```
├── pms-backend/                # 后端 Spring Boot 项目
│   ├── src/main/java/com/pms/
│   │   ├── PmsApplication.java
│   │   ├── common/             # 统一返回、异常处理、JWT、MyBatis Plus 配置
│   │   └── module/
│   │       ├── auth/           # 登录认证
│   │       ├── project/        # 项目管理（WBS、里程碑、依赖、成员）
│   │       ├── task/           # 任务管理（看板、工时、评审）
│   │       ├── requirement/    # 需求管理（评审、追溯）
│   │       ├── defect/         # 缺陷与质量管理
│   │       └── statistics/     # 统计看板
│   └── src/main/resources/
│       ├── application.yml     # 应用配置
│       └── sql/
│           ├── init.sql        # 数据库建表脚本
│           └── sample_data.sql # 示例数据
└── pms-frontend/               # 前端 Vue 3 项目
    └── src/
        ├── api/                # 后端接口封装
        ├── views/              # 页面组件
        │   ├── login/          # 登录
        │   ├── dashboard/      # 仪表盘
        │   ├── project/        # 项目列表、详情、甘特图
        │   ├── requirement/    # 需求列表
        │   ├── task/           # 看板、任务列表
        │   ├── defect/         # 缺陷、测试用例、检查清单
        │   └── stats/          # 统计分析
        ├── router/             # 路由配置
        ├── store/              # Pinia 状态管理
        └── utils/              # Axios 请求封装
```

## 快速开始

### 环境要求

- **JDK 17+**
- **Maven 3.6+**
- **MySQL 8.x**
- **Node.js 18+** 和 npm

### 1. 创建数据库

启动 MySQL 后，执行初始化脚本：

```bash
mysql -u root -p < pms-backend/src/main/resources/sql/init.sql
```

再执行 sample_data.sql 来填充示例数据：

```bash
mysql -u root -p < pms-backend/src/main/resources/sql/sample_data.sql
```

默认数据库连接配置（`application.yml`）：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/pms
    username: root
    password: 123456
```

> 如数据库用户名/密码不同，请修改 `pms-backend/src/main/resources/application.yml`。

### 2. 启动后端

```bash
cd pms-backend
mvn spring-boot:run
```

后端启动在 **http://localhost:8080**，接口文档地址：**http://localhost:8080/doc.html**

### 3. 启动前端

```bash
cd pms-frontend
npm install
npm run dev
```

前端开发服务器启动在 **http://localhost:3000**，API 请求自动代理到后端 `localhost:8080`。

### 4. 登录

- 默认管理员账号：`admin` / `admin123`
- 默认项目经理账号：`pm001` / `123456`
- 默认开发者账号：`dev001` / `123456`

（来自 sample_data.sql，请先执行该脚本）

## API 认证

系统使用 **JWT Token** 进行身份认证：

1. 调用 `POST /auth/login` 传入 `{ username, password }` 获取 token
2. 后续请求在 Header 中携带 `Authorization: Bearer <token>`
3. Token 有效期 24 小时，过期需重新登录

## 关键业务规则

| 规则 | 说明 |
|------|------|
| 项目编号 | 自动生成 `PROJ-YYYY-NNN` 格式编号 |
| WBS | 非严格树形结构，支持多层级拆分 |
| 关键路径 | 基于拓扑排序 + 正推/逆推法计算（CPM） |
| 需求版本 | 每次修改自动递增版本号 |
| 工时校验 | 单条工时 0-16h，单日累计不超过 16h |
| 逻辑删除 | 所有实体使用 MyBatis Plus 逻辑删除 |

## 许可证

本项目仅用于学习和演示目的。
