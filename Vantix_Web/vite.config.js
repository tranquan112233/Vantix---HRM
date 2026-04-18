import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'
import {fileURLToPath, URL} from 'node:url'

export default defineConfig({
    plugins: [vue()], define: {
        global: 'window',
    }, resolve: {
        alias: {'@': fileURLToPath(new URL('./src', import.meta.url))}
    }, server: {
        proxy: {
            '/api': {target: 'http://localhost:8080', changeOrigin: true},
            '/ws-hr': {target: 'http://localhost:8080', changeOrigin: true, ws: true}
        }
    }
})
