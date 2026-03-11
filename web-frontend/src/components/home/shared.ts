export const dashboardCardProps = {
  borderRadius: "28px",
  border: "1px solid",
  borderColor: "rgba(20, 32, 51, 0.08)",
  bg: "rgba(255,255,255,0.76)",
  boxShadow: "0 24px 60px rgba(18, 38, 63, 0.09)",
} as const;

export const heroCardProps = {
  ...dashboardCardProps,
  borderRadius: "32px",
  overflow: "hidden",
} as const;
