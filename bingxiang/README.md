# 智能冰箱管理系统 (Smart Fridge Management System)

一个基于 Spring Boot 和 H2 数据库的智能冰箱管理系统。

## 功能特性

### 1. 用户账号管理
- 用户注册与登录
- 密码使用 SHA-256 加密存储
- Session 会话管理

### 2. 冰箱食品库存管理
- 添加食品（名称、分类、数量、单位、购买日期、过期日期、存放位置）
- 查看库存列表
- 删除食品
- 按过期日期排序

### 3. 过期提醒功能
- 自动检测即将过期的食品（3天内）
- 可查看7天内过期物品
- 颜色区分过期程度

### 4. 菜谱推荐系统
- 根据冰箱食材智能推荐菜谱
- 显示食材匹配度百分比
- 预置6个经典家常菜谱

## 技术栈

- **后端框架**: Spring Boot 3.2.0
- **数据库**: H2 Database (文件数据库)
- **前端模板**: Thymeleaf
- **构建工具**: Maven
- **Java版本**: 17

## 项目结构

```
src/
├── main/
│   ├── java/com/smartfridge/
│   │   ├── SmartFridgeApplication.java
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   ├── IndexController.java
│   │   │   ├── InventoryController.java
│   │   │   ├── RecipeController.java
│   │   │   └── AlertController.java
│   │   ├── entity/
│   │   │   ├── User.java
│   │   │   ├── FoodCategory.java
│   │   │   ├── FoodInventory.java
│   │   │   └── Recipe.java
│   │   ├── repository/
│   │   │   ├── UserRepository.java
│   │   │   ├── FoodCategoryRepository.java
│   │   │   ├── FoodInventoryRepository.java
│   │   │   └── RecipeRepository.java
│   │   └── service/
│   │       ├── UserService.java
│   │       ├── FoodInventoryService.java
│   │       └── RecipeService.java
│   └── resources/
│       ├── application.properties
│       └── templates/
│           ├── login.html
│           ├── register.html
│           ├── index.html
│           ├── inventory.html
│           ├── add_food.html
│           ├── recipes.html
│           └── alerts.html
└── pom.xml
```

## 快速开始

### 前置要求
- JDK 17 或更高版本
- Maven 3.6+

### 构建项目

```bash
mvn clean install
```

### 运行项目

```bash
mvn spring-boot:run
```

或者运行打包后的 JAR 文件：

```bash
java -jar target/smart-fridge-system-1.0.0.jar
```

### 访问系统

打开浏览器访问: http://localhost:8080

首次访问会自动跳转到登录页面。点击"立即注册"创建账号。

## 数据库配置

系统使用 H2 文件数据库，数据存储在 `./data/smartfridge.mv.db` 文件中。

### H2 控制台
访问 http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:file:./data/smartfridge`
- 用户名: `sa`
- 密码: (空)

## 默认菜谱

系统初始化时会自动添加以下6个菜谱：
1. 番茄炒蛋
2. 红烧肉
3. 蒜蓉西兰花
4. 宫保鸡丁
5. 清蒸鲈鱼
6. 酸辣土豆丝

## 食品分类

系统初始化时会自动添加以下9个分类：
1. 新鲜蔬菜
2. 新鲜水果
3. 肉类
4. 海鲜
5. 乳制品
6. 饮料
7. 调味品
8. 剩菜剩饭
9. 其他
