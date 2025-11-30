export type AthleteUnits = {
  speedLabel?: string;
  distanceLabel?: string;
  elevationLabel?: string;
  temperatureLabel?: string;
};

export type AthleteRole = {
  name?: string;
  description?: string;
  [key: string]: unknown;
} | string | null;

export type AthleteProfile = {
  athleteId: number;
  displayName: string;
  displayLocation?: string;
  avatarUrl?: string;
  lastUpdate?: number;
  role?: AthleteRole;
  unit?: AthleteUnits;
};

export type DemoStatistic = {
  total?: number;
};

export type UserStats = {
  totalDistance?: number;
  totalElevation?: number;
  totalTime?: number;
  activityCount?: number;
  units?: AthleteUnits;
};