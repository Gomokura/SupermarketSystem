const { app, BrowserWindow, dialog } = require('electron')
const path = require('path')
const isDev = require('electron-is-dev')

let mainWindow

function createWindow() {
  // 创建浏览器窗口
  mainWindow = new BrowserWindow({
    width: 1280,
    height: 800,
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true,
      preload: path.join(__dirname, 'preload.js')
    }
  })

  // 隐藏菜单栏
  mainWindow.removeMenu()

  // 加载URL
  const url = isDev
    ? 'http://localhost:3000/pos'
    : `file://${path.join(__dirname, '../frontend/dist/index.html')}#/pos`

  mainWindow.loadURL(url).catch(err => {
    console.error('Failed to load URL:', err)
    dialog.showErrorBox(
      '启动失败',
      '请确认前后端服务已启动\n\n' +
      '需要运行:\n' +
      '1. 后端: java -jar backend/target/supermarket-backend-1.0.0.jar\n' +
      '2. 前端: npm run dev'
    )
    app.quit()
  })

  // 打开开发者工具（开发环境）
  if (isDev) {
    mainWindow.webContents.openDevTools()
  }

  mainWindow.on('closed', () => {
    mainWindow = null
  })
}

// App 事件监听
app.on('ready', createWindow)

app.on('window-all-closed', () => {
  // 在 macOS 上，应用通常保持活跃状态
  if (process.platform !== 'darwin') {
    app.quit()
  }
})

app.on('activate', () => {
  // 在 macOS 上，点击 dock 图标时重新创建窗口
  if (mainWindow === null) {
    createWindow()
  }
})

// 处理任何未捕获的异常
process.on('uncaughtException', (error) => {
  console.error('Uncaught Exception:', error)
})
