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
  lastUpdate?: string;
  role?: AthleteRole;
  unit?: "metric" | "imperial";
};

export const getAthleteUnits = (unit?: "metric" | "imperial"): AthleteUnits => {
  return unit === "imperial" ? {
    speedLabel: "mph",
    distanceLabel: "mi",
    elevationLabel: "ft",
    temperatureLabel: "°F"
  } : {
    speedLabel: "km/h",
    distanceLabel: "km",
    elevationLabel: "m",
    temperatureLabel: "°C"
  };
};

export type DemoStatistic = {
  total?: number;
};

export type UserProgress = {
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
  estimate?: UserProgress;
  progress?: UserProgress;
  commute?: UserProgress;
};

export type AchievementMetric =
  | 'maxAverageSpeed'
  | 'maxDistance'
  | 'maxTimeInSec'
  | 'maxElevation'
  | 'maxAveragePower'
  | 'maxHeartRate'
  | 'maxAverageHeartRate'
  | 'minAverageTemperature'
  | 'maxAverageTemperature';

export type AchievementEntry = {
  value?: number;
  activityId?: number;
  activityName?: string;
  activityTime?: string;
};

export type AthleteAchievements = Partial<Record<AchievementMetric, AchievementEntry>> & {
  [key: string]: AchievementEntry | undefined;
};
