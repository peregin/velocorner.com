import { createSystem, defaultConfig, defineConfig } from '@chakra-ui/react';

const customConfig = defineConfig({
  theme: {
    tokens: {
      fonts: {
        heading: { value: "'Space Grotesk', sans-serif" },
        body: { value: "'Manrope', sans-serif" },
      },
      colors: {
        slate: {
          50: { value: '#f5f7fb' },
          100: { value: '#edf1f7' },
          200: { value: '#d8e0eb' },
          300: { value: '#b7c5d6' },
          400: { value: '#8b9db4' },
          500: { value: '#64748b' },
          600: { value: '#4f5e74' },
          700: { value: '#364255' },
          800: { value: '#1f2937' },
          900: { value: '#111827' },
        },
        brand: {
          50: { value: '#eefbf7' },
          100: { value: '#d5f5ea' },
          200: { value: '#abe8d5' },
          300: { value: '#74d8bb' },
          400: { value: '#39c09d' },
          500: { value: '#17a685' },
          600: { value: '#0f856b' },
          700: { value: '#106a57' },
          800: { value: '#115448' },
          900: { value: '#103f37' },
        },
        cyan: {
          50: { value: '#eff9ff' },
          100: { value: '#d9efff' },
          200: { value: '#bbe3ff' },
          300: { value: '#8fd1ff' },
          400: { value: '#58b7ff' },
          500: { value: '#2c98f0' },
          600: { value: '#1c78ce' },
          700: { value: '#175fa6' },
          800: { value: '#194f88' },
          900: { value: '#1b436f' },
        },
      },
    },
  },
});

export const system = createSystem(defaultConfig, customConfig);
