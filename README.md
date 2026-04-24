# TraeSolo Mobile

一个基于原生 Android Java 开发的内嵌网页应用，提供流畅的 WebView 浏览体验。

## 功能特性

### 核心功能
- **内嵌网页**：默认加载 `https://solo.trae.cn/`
- **URL 持久化**：关闭 APP 后自动保存最后浏览地址，下次启动自动恢复
- **错误提示**：网页加载失败时显示友好提示页面，支持一键重试
- **首页导航**：顶部导航栏 "SOLE" 按钮，一键返回默认首页

### 用户体验
- **启动页**：冷启动时显示品牌启动页
- **进度条**：页面加载时顶部显示加载进度
- **返回导航**：支持 WebView 内历史记录回退
- **Material Design**：现代化 UI 设计风格

## 项目结构

```
app/src/main/
├── AndroidManifest.xml           # 应用清单
├── java/trae_mobile/app/com/
│   └── MainActivity.java         # 主 Activity
└── res/
    ├── drawable/                 # 图形资源
    ├── layout/                   # 布局文件
    ├── menu/                     # 菜单配置
    ├── mipmap-xxxhdpi/           # 应用图标
    ├── values/                   # 字符串、颜色、样式
    └── xml/                      # 网络安全配置
```

## 技术栈

- **开发语言**：Java
- **最低 SDK**：Android 7.0 (API 24)
- **目标 SDK**：Android 14 (API 34)
- **构建工具**：Gradle 8.0 + Android Gradle Plugin 8.1.0
- **UI 框架**：AndroidX + Material Components

## 构建说明

### 环境要求
- JDK 8 或更高版本
- Android Studio Hedgehog (2023.1.1) 或更高版本
- Android SDK 34

### 构建步骤

1. 使用 Android Studio 打开项目根目录
2. 等待 Gradle 同步完成
3. 选择 `Build > Make Project` 或按 `Ctrl+F9`

### 打包 Release APK

项目已配置打包证书：
- 证书文件：`trae_mobile.app.com.keystore`
- 证书密码：`123456`
- 应用包名：`trae_mobile.app.com`

在 Android Studio 中选择 `Build > Generate Signed Bundle/APK` 进行打包。

## 权限说明

| 权限 | 用途 |
|------|------|
| `INTERNET` | 网络访问，加载网页内容 |
| `ACCESS_NETWORK_STATE` | 检测网络连接状态 |

## 仓库地址

[https://github.com/tangping-cmd/trae_mobile](https://github.com/tangping-cmd/trae_mobile)

## License

MIT License
