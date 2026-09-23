import { act, cleanup, fireEvent, render, screen } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { Provider } from "../ui/provider";
import ActivityTerrainMap from "./ActivityTerrainMap";

const mocks = vi.hoisted(() => ({
  options: [] as any[],
  maps: [] as any[],
  fail: false,
}));

vi.mock("maplibre-gl", () => ({
  setWorkerUrl: vi.fn(),
  Map: class {
    handlers: Record<string, (...args: any[]) => void> = {};
    remove = vi.fn();
    resize = vi.fn();
    fitBounds = vi.fn();
    addControl = vi.fn();
    constructor(options: any) {
      if (mocks.fail) throw new Error("WebGL unavailable");
      mocks.options.push(options);
      mocks.maps.push(this);
    }
    once(name: string, handler: (...args: any[]) => void) { this.handlers[name] = handler; }
    on(name: string, handler: (...args: any[]) => void) { this.handlers[name] = handler; }
  },
  NavigationControl: class {},
  AttributionControl: class {},
  LngLatBounds: class { extend() { return this; } },
}));

const points = [{ lat: 46.7, lon: 8.1 }, { lat: 46.8, lon: 8.2 }];
const view = (route = points) => <Provider><ActivityTerrainMap points={route} /></Provider>;

beforeEach(() => {
  mocks.options.length = 0;
  mocks.maps.length = 0;
  mocks.fail = false;
});
afterEach(() => { cleanup(); vi.restoreAllMocks(); });

describe("ActivityTerrainMap", () => {
  it("renders a route without altitude data and releases the map on unmount", () => {
    const { unmount } = render(view());
    expect(mocks.options[0].style.sources.route.data.geometry.coordinates).toEqual([[8.1, 46.7], [8.2, 46.8]]);
    expect(mocks.options[0].style.terrain).toEqual({ source: "terrain", exaggeration: 1.3 });
    act(() => mocks.maps[0].handlers.idle());
    expect(screen.queryByLabelText("Loading terrain")).toBeNull();
    fireEvent.click(screen.getByText("Reset view"));
    expect(mocks.maps[0].fitBounds).toHaveBeenCalledTimes(2);
    unmount();
    expect(mocks.maps[0].remove).toHaveBeenCalledOnce();
  });

  it("keeps dateline crossings local and rebuilds the map when the route changes", () => {
    const { rerender } = render(view([{ lat: 10, lon: 179.9 }, { lat: 10.1, lon: -179.9 }]));
    const coordinates = mocks.options[0].style.sources.route.data.geometry.coordinates;
    expect(coordinates[1][0]).toBeCloseTo(180.1);
    rerender(view());
    expect(mocks.maps[0].remove).toHaveBeenCalledOnce();
    expect(mocks.maps).toHaveLength(2);
  });

  it("offers a retry after a tile failure and cleans up the failed map", () => {
    vi.spyOn(console, "error").mockImplementation(() => {});
    render(view());
    act(() => mocks.maps[0].handlers.error({ error: new Error("Network failure") }));
    expect(screen.getByRole("status").textContent).toContain("could not be loaded");
    fireEvent.click(screen.getByText("Retry"));
    expect(mocks.maps[0].remove).toHaveBeenCalledOnce();
    expect(mocks.maps).toHaveLength(2);
  });

  it("preserves the card with a useful message when WebGL cannot start", () => {
    mocks.fail = true;
    render(view());
    expect(screen.getByRole("status").textContent).toContain("WebGL");
    mocks.fail = false;
    fireEvent.click(screen.getByText("Retry"));
    expect(mocks.maps).toHaveLength(1);
  });
});
