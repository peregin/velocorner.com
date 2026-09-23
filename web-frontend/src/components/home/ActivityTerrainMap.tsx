import { useEffect, useRef, useState } from "react";
import { Box, Button, HStack, Spinner, Text } from "@chakra-ui/react";
import { AttributionControl, LngLatBounds, Map, NavigationControl, setWorkerUrl } from "maplibre-gl";
import terrainWorkerUrl from "maplibre-gl/dist/maplibre-gl-worker.mjs?worker&url";
import "maplibre-gl/dist/maplibre-gl.css";

// Bundle the module worker explicitly so its URL survives Vite chunking.
setWorkerUrl(terrainWorkerUrl);

const SLOW_TERRAIN_MESSAGE = "Terrain is taking longer than expected to load. You can retry the map.";

type RoutePoint = { lat: number; lon: number };

// Keep adjacent points in the same world copy when a route crosses the dateline.
const routeCoordinates = (points: readonly RoutePoint[]): [number, number][] => {
  const coordinates: [number, number][] = [];
  for (const point of points) {
    if (!Number.isFinite(point.lon) || !Number.isFinite(point.lat) || Math.abs(point.lat) > 85.051129) continue;
    const previousLongitude = coordinates[coordinates.length - 1]?.[0] ?? point.lon;
    const longitude = point.lon + 360 * Math.round((previousLongitude - point.lon) / 360);
    coordinates.push([longitude, point.lat]);
  }
  return coordinates;
};

const ActivityTerrainMap = ({ points }: { points: readonly RoutePoint[] }) => {
  const container = useRef<HTMLDivElement>(null);
  const resetView = useRef<() => void>(() => {});
  const [attempt, setAttempt] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!container.current) return;
    setLoading(true);
    setError(null);
    const coordinates = routeCoordinates(points);
    if (coordinates.length < 2) {
      setLoading(false);
      setError("This route cannot be displayed on the terrain map.");
      return;
    }

    let map: Map;
    try {
      map = new Map({
        container: container.current,
        // No basemap account or API key is needed for this shaded relief view.
        style: {
          version: 8,
          sources: {
            terrain: {
              type: "raster-dem",
              url: "https://tiles.mapterhorn.com/tilejson.json",
              encoding: "terrarium",
              tileSize: 512,
            },
            // Hillshade and terrain use independent tile zoom levels.
            hillshade: {
              type: "raster-dem",
              url: "https://tiles.mapterhorn.com/tilejson.json",
              encoding: "terrarium",
              tileSize: 512,
            },
            route: {
              type: "geojson",
              data: { type: "Feature", properties: {}, geometry: { type: "LineString", coordinates } },
            },
            endpoints: {
              type: "geojson",
              data: {
                type: "FeatureCollection",
                features: [coordinates[0], coordinates[coordinates.length - 1]].map((coordinate, index) => ({
                  type: "Feature",
                  properties: { endpoint: index === 0 ? "start" : "finish" },
                  geometry: { type: "Point", coordinates: coordinate },
                })),
              },
            },
          },
          layers: [
            { id: "background", type: "background", paint: { "background-color": "#dce5cf" } },
            {
              id: "relief", type: "hillshade", source: "hillshade",
              paint: {
                "hillshade-exaggeration": 0.65,
                "hillshade-shadow-color": "#344b3c",
                "hillshade-highlight-color": "#fff7df",
                "hillshade-accent-color": "#607552",
              },
            },
            {
              id: "route-outline", type: "line", source: "route",
              layout: { "line-cap": "round", "line-join": "round" },
              paint: { "line-color": "#ffffff", "line-width": 7 },
            },
            {
              id: "route-line", type: "line", source: "route",
              layout: { "line-cap": "round", "line-join": "round" },
              paint: { "line-color": "#b91c1c", "line-width": 4 },
            },
            {
              id: "endpoints", type: "circle", source: "endpoints",
              paint: {
                "circle-radius": 6, "circle-stroke-width": 2, "circle-stroke-color": "#ffffff",
                "circle-color": ["match", ["get", "endpoint"], "start", "#16a34a", "#f97316"],
              },
            },
          ],
          terrain: { source: "terrain", exaggeration: 1.3 },
        },
        center: coordinates[0],
        zoom: 10,
        pitch: 55,
        maxPitch: 70,
        attributionControl: false,
        cooperativeGestures: true,
      });
    } catch {
      setLoading(false);
      setError("The 3D map could not start. Check that WebGL is enabled in your browser.");
      container.current.replaceChildren();
      return;
    }

    map.addControl(new NavigationControl({ visualizePitch: true }), "top-right");
    map.addControl(new AttributionControl({ compact: true }), "bottom-right");
    const bounds = coordinates.reduce((result, coordinate) => result.extend(coordinate), new LngLatBounds());
    const fitRoute = () => map.fitBounds(bounds, { padding: 48, maxZoom: 14, pitch: 55, bearing: -20, duration: 0 });
    resetView.current = fitRoute;
    fitRoute();

    const timeout = window.setTimeout(() => {
      setLoading(false);
      setError(SLOW_TERRAIN_MESSAGE);
    }, 20000);
    map.once("idle", () => {
      window.clearTimeout(timeout);
      setLoading(false);
      setError((message) => message === SLOW_TERRAIN_MESSAGE ? null : message);
    });
    map.on("error", (event) => {
      window.clearTimeout(timeout);
      setLoading(false);
      setError("Some terrain could not be loaded. You can retry the map.");
      console.error("Activity terrain map:", event.error);
    });
    const observer = new ResizeObserver(() => map.resize());
    observer.observe(container.current);

    return () => {
      window.clearTimeout(timeout);
      observer.disconnect();
      resetView.current = () => {};
      map.remove();
    };
  }, [points, attempt]);

  return (
    <Box position="relative" minH="320px" borderRadius="24px" overflow="hidden" bg="#dce5cf">
      {/* MapLibre's unlayered CSS overrides Chakra's layered position styles. */}
      <div
        ref={container}
        style={{ position: "absolute", inset: 0, width: "100%", height: "100%" }}
        role="region"
        aria-label="Interactive 3D terrain and activity route"
      />
      <HStack position="absolute" top={3} left={3} gap={2}>
        <Button size="xs" variant="solid" bg="white" color="gray.800" onClick={() => resetView.current()}>Reset view</Button>
        {loading && <Spinner size="sm" aria-label="Loading terrain" />}
      </HStack>
      <HStack position="absolute" bottom={8} left={3} bg="whiteAlpha.900" borderRadius="md" px={2} py={1} fontSize="xs" gap={3} pointerEvents="none">
        <Text color="green.700">● Start</Text><Text color="orange.700">● Finish</Text>
      </HStack>
      {error && (
        <Box position="absolute" top={14} left={3} right={12} bg="white" borderRadius="md" p={3} role="status">
          <Text fontSize="sm" color="gray.800">{error}</Text>
          <Button size="xs" mt={2} onClick={() => setAttempt((value) => value + 1)}>Retry</Button>
        </Box>
      )}
    </Box>
  );
};

export default ActivityTerrainMap;
