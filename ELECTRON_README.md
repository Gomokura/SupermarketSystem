# 超市收银系统 - Electron 桌面应用

## 项目结构

```
SupermarketSystem/
├── frontend/                    # Vue 3 + Vite 前端项目
│   ├── src/
│   │   ├── views/
│   │   │   └── cashier/        # 收银端组件
│   │   └── router/
│   │       └── index.js        # 路由配置 (路由: /pos)
│   └── package.json
├── backend/                     # Spring Boot 后端
│   ├── src/
│   └── target/
├── electron/
│   ├── main.js                  # Electron 主进程
│   └── preload.js               # 安全隔离脚本
├── package.json                 # 根目录配置（Electron）
└── README.md
```

## 快速开始

### 1. 安装依赖

```bash
# 在项目根目录安装 Electron 依赖
npm install

# 进入前端目录安装前端依赖
cd frontend
npm install
cd ..
```

### 2. 运行开发环境

需要同时启动 3 个服务：

**终端1 - 启动后端服务（Spring Boot）**
```bash
cd backend
java -jar target/supermarket-backend-1.0.0.jar
# 或使用 Maven
mvn clean spring-boot:run
```

**终端2 - 启动前端开发服务器**
```bash
cd frontend
npm run dev
# 前端会在 http://localhost:3000 启动
```

**终端3 - 启动 Electron 应用**
```bash
npm run electron:dev
# 会自动打开 Electron 窗口，加载 http://localhost:3000/pos
```

### 3. 构建生产版本

首先构建前端：
```bash
cd frontend
npm run build
# 输出到 frontend/dist/
```

然后构建 Electron 应用：
```bash
npm run electron:build:win
# 输出到 dist-electron/ 目录
```

生成的安装包位置：
```
dist-electron/超市收银系统 Setup 1.0.0.exe
```

## 可用命令

### 根目录命令

| 命令 | 说明 |
|------|------|
| `npm install` | 安装 Electron 依赖 |
| `npm run dev` | 启动前端开发服务器 |
| `npm run build` | 构建前端 |
| `npm run electron` | 启动 Electron（生产环境） |
| `npm run electron:dev` | 启动 Electron（开发环境，带调试） |
| `npm run electron:build` | 构建 Electron 应用 |
| `npm run electron:build:win` | 构建 Windows 安装包 |
| `npm run cashier:dev` | 快捷命令：启动前端 dev 服务 |
| `npm run cashier:build` | 快捷命令：构建完整应用 |

### 前端目录命令

在 `frontend/` 目录中运行：

| 命令 | 说明 |
|------|------|
| `npm run dev` | 启动开发服务器（:3000） |
| `npm run build` | 构建生产版本 |
| `npm run preview` | 预览生产版本 |

## 配置说明

### Electron 主进程 (electron/main.js)

- **窗口大小**: 1280x800
- **窗口标题**: 超市收银系统
- **开发URL**: http://localhost:3000/pos
- **生产URL**: file:///.../dist/index.html#/pos
- **菜单**: 隐藏
- **错误提示**: 启动失败时提示检查服务

### Build 配置 (package.json)

- **appId**: com.supermarket.cashier
- **productName**: 超市收银系统
- **目标**: Windows NSIS 安装包
- **输出**: dist-electron/
- **特性**:
  - 创建桌面快捷方式
  - 允许自定义安装目录
  - 支持卸载

## 注意事项

1. **开发模式**
   - 前端开发服务器必须运行在 http://localhost:3000
   - 后端 API 必须运行在 http://localhost:8080
   - Electron 会自动连接到这两个服务

2. **生产模式**
   - 需要先构建前端（生成 frontend/dist）
   - Electron 会加载本地打包的前端文件
   - 后端仍需要独立部署或与应用打包

3. **打包依赖**
   ```
   electron: 最新版本
   electron-builder: 24.6.4
   electron-is-dev: 2.0.0
   cross-env: 7.0.3
   ```

4. **调试**
   - 开发环境会自动打开 DevTools
   - 可以在 DevTools 中调试前端代码
   - Electron 主进程错误会在控制台显示

## 常见问题

### Q: 启动时提示 "请确认前后端服务已启动"
**A:** 检查以下几点：
- 后端服务是否运行在 http://localhost:8080
- 前端开发服务器是否运行在 http://localhost:3000
- 防火墙是否阻止了本地连接
- 网络接口是否正常

### Q: 打包生成的 exe 如何与后端配合？
**A:** 
- 开发阶段：后端单独运行
- 生产部署：可以将后端 jar 与前端打包一起发布
- 或者配置打包脚本自动启动后端进程（需要额外配置）

### Q: 能否在其他电脑上运行生成的 exe？
**A:** 可以，exe 包含了所有必要的依赖，但：
- 后端服务仍需单独运行或部署
- 需要 Java 环境运行后端（如果后端单独运行）
- 可以考虑使用 electron-builder 的 NSIS 配置自动部署整个系统

## 技术栈

- **前端**: Vue 3, Vite, Element Plus
- **后端**: Spring Boot 3.2, Java 21
- **桌面框架**: Electron 28+
- **打包工具**: electron-builder

## 许可证

MIT
