import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3111,
    cors: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8111',
        changeOrigin: true
      }
    }
  }
})
