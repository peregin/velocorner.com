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

export type UserStatsBreakdown = {
  days?: number;
  rides?: number;
  distance?: number;
  longestDistance?: number;
  movingTime?: number;
  averageSpeed?: number;
  elevation?: number;
  longestElevation?: number;
};

export type UserStats = {
  yearlyPercentile?: number;
  estimate?: UserStatsBreakdown;
  progress?: UserStatsBreakdown;
  commute?: UserStatsBreakdown;
};
