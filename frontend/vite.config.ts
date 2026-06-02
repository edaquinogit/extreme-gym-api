/// <reference types="vitest/config" />
import { createRequire } from 'node:module'
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

const require = createRequire(import.meta.url)

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  test: {
    environment: 'jsdom',
    setupFiles: './src/test/setup.ts',
  },
  resolve: {
    alias: [
      {
        find: 'fast-equals',
        replacement: require.resolve('fast-equals'),
      },
    ],
  },
})
