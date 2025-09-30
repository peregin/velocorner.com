import { createSystem, defaultConfig, defineConfig } from '@chakra-ui/react';

const customConfig = defineConfig({
  theme: {
    tokens: {
      colors: {
        brand: {
          50: { value: '#e6f7ff' },
          100: { value: '#bae7ff' },
          200: { value: '#91d5ff' },
          300: { value: '#69c0ff' },
          400: { value: '#40a9ff' },
          500: { value: '#1890ff' },
          600: { value: '#0070f3' },
          700: { value: '#0050b3' },
          800: { value: '#003a8c' },
          900: { value: '#002766' },
        },
        cyan: {
          50: { value: '#e0f7fa' },
          100: { value: '#b2ebf2' },
          200: { value: '#80deea' },
          300: { value: '#4dd0e1' },
          400: { value: '#26c6da' },
          500: { value: '#00bcd4' },
          600: { value: '#00acc1' },
          700: { value: '#0097a7' },
          800: { value: '#00838f' },
          900: { value: '#006064' },
        },
      },
    },
  },
});

export const system = createSystem(defaultConfig, customConfig);