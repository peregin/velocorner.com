export const getInitials = (value?: string) => {
  if (!value) return undefined;
  const initials = value
    .split(" ")
    .map((part) => part.trim().charAt(0))
    .filter(Boolean)
    .join("")
    .slice(0, 2)
    .toUpperCase();
  return initials || undefined;
};

export const formatTimestamp = (timestamp?: number) => {
  if (!timestamp) return null;
  const milliseconds = timestamp > 1_000_000_000_000 ? timestamp : timestamp * 1000;
  const date = new Date(milliseconds);
  if (Number.isNaN(date.getTime())) return null;
  return date.toLocaleString();
};