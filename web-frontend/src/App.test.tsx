import { render } from '@testing-library/react';
import { Provider } from './components/ui/provider';
import App from './App';

it('renders without crashing', () => {
  render(
    <Provider>
      <App />
    </Provider>
  );
});
