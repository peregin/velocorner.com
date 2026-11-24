import { readFileSync } from "node:fs";
import { cleanup, render } from "@testing-library/react";
import { afterEach, expect, it } from "vitest";
import PageMeta from "./PageMeta";

afterEach(cleanup);

it("does not declare a shared canonical in the HTML shell", () => {
  const html = readFileSync("index.html", "utf8");
  expect(html).not.toMatch(/<link\b[^>]*rel=["']canonical["']/i);
});

it("keeps one page-specific canonical as routes change and removes it on unmount", () => {
  const links = () => document.head.querySelectorAll('link[rel="canonical"]');
  const { rerender, unmount } = render(
    <PageMeta title="Home" canonical="https://velocorner.com/" />,
  );
  expect(links()).toHaveLength(1);
  expect(links()[0].getAttribute("href")).toBe("https://velocorner.com/");

  rerender(<PageMeta title="About" canonical="https://velocorner.com/about" />);
  expect(links()).toHaveLength(1);
  expect(links()[0].getAttribute("href")).toBe("https://velocorner.com/about");

  rerender(<PageMeta title="Not found" />);
  expect(links()).toHaveLength(0);

  rerender(<PageMeta title="Privacy" canonical="https://velocorner.com/privacy" />);
  expect(links()).toHaveLength(1);
  expect(links()[0].getAttribute("href")).toBe("https://velocorner.com/privacy");
  unmount();
  expect(links()).toHaveLength(0);
});
