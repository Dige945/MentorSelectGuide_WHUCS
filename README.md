# MentorSelectGuide_WHUCS

这是一个基于Vue 3和Spring Boot开发的武汉大学计算机学院导师指南系统，旨在为学生科研之路提供智能化指南。

## 项目特点

- 基于DeepSeek API的智能推荐
- 实时流式对话响应
- 前后端分离架构
- RESTful API设计

## 技术栈

### 前端
- **框架**: Vue 3
- **UI组件库**: Element Plus
- **路由管理**: Vue Router 4
- **HTTP客户端**: Axios
- **图标库**: Element Plus Icons

### 后端
- **框架**: Spring Boot
- **API集成**: DeepSeek API
- **数据库**: MySQL (计划中)
- **模板引擎**: Thymeleaf
- **构建工具**: Maven

## 系统功能

### 已实现功能
- AI智能对话（基于SSE流式响应）
- 智能导师推荐
- 基础页面框架

### 待实现功能
- 教师信息浏览与搜索
- 课程信息查询
- 教师评价提交与查看
- 教师与课程排行榜
- 用户登录注册系统
- 个人中心

## 项目结构

```
MentorSelectGuide_WHUCS/
├── UI_vue/                  # 前端项目
│   └── ui/
│       ├── public/          # 静态资源
│       ├── src/
│       │   ├── assets/      # 资源文件
│       │   ├── components/  # 通用组件
│       │   ├── router/      # 路由配置
│       │   ├── views/       # 页面组件
│       │   ├── App.vue      
│       │   └── main.js      
│       └── package.json     
│
└── src/                     # 后端项目
    ├── main/
    │   ├── java/
    │   │   └── com/example/whucs_mentorguide/
    │   │       ├── controller/    # 控制器
    │   │       ├── service/       # 服务层
    │   │       ├── model/         # 数据模型
    │   │       └── config/        # 配置类
    │   └── resources/
    │       ├── application.properties  # 应用配置
    │       └── templates/         # 模板文件
    └── test/                # 测试目录
```

## 快速开始

### 前端启动
```bash
cd UI_vue/ui
npm install
npm run serve
```

### 后端启动
```bash
# 使用Maven启动Spring Boot应用
./mvnw spring-boot:run
```

## 配置说明

### DeepSeek API配置
在 `application.properties` 中配置：
```properties
ai.config.deepseek.apiKey=your_api_key
ai.config.deepseek.baseUrl=https://api.deepseek.com/v1
```

### 数据库配置
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/<your-database-name>
spring.datasource.username=<username>
spring.datasource.password=<password>
```

## 浏览器兼容性

- Chrome (最新版本)
- Firefox (最新版本)
- Edge (最新版本)
- Safari (最新版本)

## 开发计划

### 近期计划
1. 完善AI推荐系统
2. 实现用户认证系统
3. 开发教师详情页
4. 实现评价提交功能
5. 数据库集成

### 长期计划
1. 优化AI对话体验
2. 添加数据分析功能
3. 实现移动端适配
4. 添加管理员后台
5. 优化系统性能

## 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

## 许可证

本项目采用 MIT 许可证
