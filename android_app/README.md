# NovaGlide Android 客户端

NovaGlide 是山东大学智能体项目的 Android 客户端应用，旨在为学生提供考研、保研、留学和考公等相关资讯和智能问答服务。

## 技术栈

- 开发语言：Kotlin
- UI 框架：Jetpack Compose
- 网络请求：Retrofit + Gson
- 图片加载：Coil
- 本地数据库：Room
- 导航组件：Navigation Compose

## 开发环境设置

### 要求
- Android Studio Flamingo (2022.2.1) 或更高版本
- JDK 17 或更高版本
- Android SDK 33 (Android 13) 或更高版本

### 配置步骤

1. 克隆仓库：
```bash
git clone https://github.com/your-org/novaglide.git
cd novaglide/android_app
```

2. 在根目录创建 `local.properties` 文件，参考 `local.properties.example` 配置你的 Android SDK 路径：
```properties
sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
```

3. 在 Android Studio 中打开项目：
   - 启动 Android Studio
   - 选择 "Open an existing project"
   - 浏览并选择 `novaglide/android_app` 目录
   - 等待 Gradle 同步完成

4. 运行应用：
   - 选择一个设备或模拟器
   - 点击运行按钮或使用快捷键 `Shift+F10`

## 应用架构

应用采用 Clean Architecture 结构，遵循 MVVM 设计模式，分为以下几层：

- **UI 层**: 包含 Jetpack Compose 界面和 ViewModel
- **Domain 层**: 包含业务逻辑和用例
- **Data 层**: 包含数据源和仓库实现

## 功能模块

- **资讯浏览**: 查看保研、考研、留学、考公相关的最新资讯
- **智能问答**: 借助 RAGflow 技术，提供智能、准确的问答服务
- **个人中心**: 收藏管理、浏览历史、偏好设置等个人功能

## 开发规范

1. 遵循 Kotlin 官方代码风格指南
2. 使用 Jetpack Compose 进行 UI 开发
3. UI 状态管理使用 ViewModel 和 StateFlow
4. 异步操作使用 Kotlin 协程
5. 依赖注入采用 Hilt (可选) 