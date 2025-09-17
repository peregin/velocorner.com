import React from 'react';
import { createRoot } from 'react-dom/client';
import { Provider } from '@/components/ui/provider';
import './index.css';
import App from './App';

const container = document.getElementById('root');
const root = createRoot(container);
root.render(
  <Provider>
    <App />
  </Provider>
);
