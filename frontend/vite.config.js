import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    host: true, // 监听 0.0.0.0，避免 Windows 上 localhost 只连 IPv6 导致拒绝连接
    port: 3000,
    strictPort: false, // 3000 被占用时自动换端口，注意看终端里实际地址
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})
