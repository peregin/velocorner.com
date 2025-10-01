import { createRoot } from 'react-dom/client';
import { Provider } from '@/components/ui/provider';
import { system } from './theme';
import './index.css';
import App from './App.tsx';

const container = document.getElementById('root');
const root = createRoot(container);
root.render(
  <Provider value={system}>
    <App />
  </Provider>
);
