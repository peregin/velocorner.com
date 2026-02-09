import { render } from '@testing-library/react';
import { it } from 'vitest';
import { act } from '@testing-library/react';
import { Provider } from './components/ui/provider';
import App from './App';

it('renders without crashing', () => {
  act(() => {
    render(
      <Provider>
        <App />
      </Provider>
    );
  });
});
