import { defineConfig } from 'vite';

export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      }
    },
    host: '0.0.0.0',
    strictPort: false,
    cors: true,
    hmr: {
      clientPort: 443
    },
    allowedHosts: ['all']
  }
}); 