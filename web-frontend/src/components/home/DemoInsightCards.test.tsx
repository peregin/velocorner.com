import { render, screen } from "@testing-library/react";
import { expect, it, vi } from "vitest";
import { Provider } from "../ui/provider";
import DemoInsightCards from "./DemoInsightCards";

vi.mock("./ActivityTerrainMap", () => ({
  default: ({ points }: { points: ReadonlyArray<{ lat: number; lon: number }> }) => (
    <div
      data-testid="demo-terrain-map"
      data-point-count={points.length}
      data-start={`${points[0].lat},${points[0].lon}`}
      data-finish={`${points[points.length - 1].lat},${points[points.length - 1].lon}`}
    />
  ),
}));

it("renders the shared terrain map with a Swiss demo route", async () => {
  render(
    <Provider>
      <DemoInsightCards distanceLabel="km" elevationLabel="m" />
    </Provider>,
  );

  const map = await screen.findByTestId("demo-terrain-map");
  expect(map.getAttribute("data-point-count")).toBe("15");
  expect(map.getAttribute("data-start")).toBe("46.6244,8.0414");
  expect(map.getAttribute("data-finish")).toBe("46.6248,8.0432");
});
