// src/setupTests.js
import { beforeAll, vi } from 'vitest';

// Mock ApexCharts to prevent SVG bbox errors
vi.mock('apexcharts', () => ({
  default: vi.fn().mockImplementation(() => ({
    render: vi.fn(),
    updateOptions: vi.fn(),
    updateSeries: vi.fn(),
    destroy: vi.fn(),
    toggleDataPointSelection: vi.fn(),
    resetSeries: vi.fn(),
  }))
}));

// Mock Highcharts
vi.mock('highcharts', () => ({
  default: new Proxy({
    Chart: vi.fn().mockImplementation(() => ({})),
    stockChart: vi.fn().mockImplementation(() => ({})),
    mapChart: vi.fn().mockImplementation(() => ({})),
    setOptions: vi.fn(),
    theme: {},
    SeriesRegistry: {}
  }, {
    get(target, prop) {
      if (!(prop in target)) {
        target[prop] = {};
      }
      return target[prop];
    }
  })
}));

// Mock highcharts modules to prevent initialization errors
vi.mock('highcharts/modules/wordcloud', () => ({
  default: vi.fn()
}));

vi.mock('highcharts/modules/datagrouping', () => ({
  default: vi.fn()
}));

vi.mock('highcharts/modules/windbarb', () => ({
  default: vi.fn()
}));

vi.mock('highcharts/modules/pattern-fill', () => ({
  default: vi.fn()
}));

vi.mock('highcharts/modules/data', () => ({
  default: vi.fn()
}));

vi.mock('highcharts/highcharts-3d', () => ({
  default: vi.fn()
}));

// Mock React charting libraries
vi.mock('highcharts-react-official', () => ({
  default: () => null
}));

vi.mock('react-apexcharts', () => ({
  default: () => null
}));

beforeAll(() => {
  // Mock window.matchMedia
  Object.defineProperty(window, 'matchMedia', {
    writable: true,
    value: vi.fn().mockImplementation(query => ({
      matches: false,
      media: query,
      onchange: null,
      addListener: vi.fn(), // deprecated
      removeListener: vi.fn(), // deprecated
      addEventListener: vi.fn(),
      removeEventListener: vi.fn(),
      dispatchEvent: vi.fn(),
    })),
  });

  // Mock window.IntersectionObserver
  Object.defineProperty(window, 'IntersectionObserver', {
    writable: true,
    value: class IntersectionObserver {
      constructor() {}
      observe() {}
      disconnect() {}
      unobserve() {}
    },
  });

  // Mock window.ResizeObserver
  Object.defineProperty(window, 'ResizeObserver', {
    writable: true,
    value: class ResizeObserver {
      constructor() {}
      observe() {}
      disconnect() {}
      unobserve() {}
    },
  });

  // Mock console.warn to suppress specific warnings during tests
  const originalWarn = console.warn;
  console.warn = (...args) => {
    // Suppress warnings about nested <p> tags and act() warnings
    const message = args.join(' ');
    if (
      message.includes('cannot be a descendant of <p>') ||
      message.includes('cannot contain a nested <p>') ||
      message.includes('update to CarouselRoot inside a test was not wrapped in act') ||
      message.includes('input of type text with both value and defaultValue props') ||
      message.includes('ReactDOMTestUtils.act is deprecated')
    ) {
      return;
    }
    originalWarn(...args);
  };
});
