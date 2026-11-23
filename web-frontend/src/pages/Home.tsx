import { type KeyboardEvent, useCallback, useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Box, Card, Flex, Image, Progress, Text, VStack } from "@chakra-ui/react";
import ApiClient from "@/service/ApiClient";
import { useAuth } from "@/service/auth";
import { toaster } from "@/components/ui/toaster";
import Weather from "@/components/charts/Weather";
import { getAthleteUnits } from "@/types/athlete";
import { useAthleteProfile } from "@/service/useAthleteProfile";
import GuestHero from "@/components/home/GuestHero";
import HomeDemoDashboard from "@/components/home/DemoDashboard";
import ActivityTypeSelector from "@/components/home/ActivityTypeSelector";
import ProfileOverview, { type SearchSuggestion } from "@/components/home/ProfileOverview";
import AnalyticsSection from "@/components/home/AnalyticsSection";
import LastActivityRoute3D from "@/components/home/LastActivityRoute3D";
import WindyMap from "@/components/home/WindyMap";
import { dashboardCardProps } from "@/components/home/shared";
import PageMeta from "@/components/PageMeta";

const showError = (title: string, description: string) => {
  toaster.create({ title, description, type: "error", duration: 5000 });
};

const Home = () => {
  const [activityTypes, setActivityTypes] = useState<string[]>(["Ride"]);
  const [selectedActivityType, setSelectedActivityType] = useState("Ride");
  const [loading, setLoading] = useState(true);
  const [logoutLoading, setLogoutLoading] = useState(false);
  const [refreshLoading, setRefreshLoading] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [searchSuggestions, setSearchSuggestions] = useState<SearchSuggestion[]>([]);
  const [suggestionsLoading, setSuggestionsLoading] = useState(false);
  const searchTimeoutRef = useRef<number | null>(null);
  const { isAuthenticated, authLoading, connect, logout } = useAuth();
  const { athleteProfile, profileLoading } = useAthleteProfile(isAuthenticated, true);
  const navigate = useNavigate();

  useEffect(() => {
    if (authLoading || !isAuthenticated) return;
    let current = true;
    setLoading(true);
    ApiClient.activityTypes()
      .then((types: string[]) => { if (current) setActivityTypes(types); })
      .catch((error: unknown) => {
        if (!current) return;
        console.error("Error fetching activity types:", error);
        showError("Error", "Failed to load activity types. Please try again.");
      })
      .finally(() => { if (current) setLoading(false); });
    return () => { current = false; };
  }, [authLoading, isAuthenticated]);

  const handleActivityTypeChange = (activityType: string) => {
    setSelectedActivityType(activityType);
  };

  const handleLogout = async () => {
    setLogoutLoading(true);
    await logout();
    setLogoutLoading(false);
  };

  const handleRefresh = async () => {
    try {
      setRefreshLoading(true);
      await ApiClient.refreshActivities();
      window.location.reload();
    } catch (error) {
      console.error("Error refreshing activities:", error);
      showError("Refresh Failed", "Unable to refresh activities. Please try again.");
      setRefreshLoading(false);
    }
  };

  const handleUnitsChange = async (selectedUnit: "metric" | "imperial") => {
    try {
      await ApiClient.updateUnits(selectedUnit);
      window.location.reload();
    } catch (error) {
      console.error("Error updating units:", error);
      showError("Update Failed", "Unable to update units. Please try again.");
    }
  };

  const fetchSuggestions = useCallback(async (query: string) => {
    if (!query.trim() || !isAuthenticated) {
      setSearchSuggestions([]);
      return;
    }

    try {
      setSuggestionsLoading(true);
      const response = await ApiClient.suggestActivities(query);

      if (!response?.suggestions) {
        setSearchSuggestions([]);
        return;
      }

      const suggestions = response.suggestions.map((suggestion: any) => {
        try {
          const activity = typeof suggestion.data === "string" ? JSON.parse(suggestion.data) : suggestion.data;
          return {
            value: suggestion.value || activity.name,
            label: suggestion.value || activity.name,
            activity,
          };
        } catch {
          return {
            value: suggestion.value,
            label: suggestion.value,
            activity: null,
          };
        }
      });

      setSearchSuggestions(suggestions);
    } catch (error) {
      console.error("Error fetching suggestions:", error);
      setSearchSuggestions([]);
    } finally {
      setSuggestionsLoading(false);
    }
  }, [isAuthenticated]);

  const handleSearchInputChange = (value: string) => {
    setSearchQuery(value);

    if (searchTimeoutRef.current) {
      clearTimeout(searchTimeoutRef.current);
    }

    if (value.trim().length >= 2) {
      searchTimeoutRef.current = window.setTimeout(() => {
        fetchSuggestions(value);
      }, 300);
      return;
    }

    setSearchSuggestions([]);
  };

  const handleSuggestionSelect = (selectedValue: string) => {
    const suggestion = searchSuggestions.find((entry) => entry.value === selectedValue);
    if (suggestion?.activity?.id) {
      navigate(`/search?aid=${suggestion.activity.id}`);
      return;
    }

    navigate(`/search?q=${encodeURIComponent(selectedValue.trim())}`);
  };

  const handleSearch = () => {
    if (searchQuery.trim()) {
      navigate(`/search?q=${encodeURIComponent(searchQuery.trim())}`);
    }
  };

  const handleSearchKeyPress = (event: KeyboardEvent) => {
    if (event.key === "Enter") {
      handleSearch();
    }
  };

  useEffect(() => {
    return () => {
      if (searchTimeoutRef.current) {
        clearTimeout(searchTimeoutRef.current);
      }
    };
  }, []);

  const profileUnits = getAthleteUnits(athleteProfile?.unit);
  const demoUnits = getAthleteUnits("metric");

  return (
    <Box maxW="1280px" mx="auto" px={{ base: 4, md: 6 }} pb={{ base: 2, md: 4 }}>
      <PageMeta
        title="Velocorner | Endurance Cycling Analytics & Strava Cockpit"
        description="Velocorner turns your Strava activity history into a concise performance cockpit with season comparisons, elevation heatmaps, and cycling statistics."
        canonical="https://velocorner.com/"
      />
      <VStack gap={{ base: 6, md: 8 }} align="stretch">
        {!isAuthenticated && (
          <GuestHero authLoading={authLoading} onConnect={connect} demoUnits={demoUnits} />
        )}

        <Card.Root {...dashboardCardProps}>
          <Card.Body p={{ base: 4, md: 6 }}>
            <Weather />
          </Card.Body>
        </Card.Root>

        {!isAuthenticated && (
          <HomeDemoDashboard
            units={profileUnits}
          />
        )}

        {isAuthenticated && (
          <>
            <Flex direction={{ base: "column", md: "row" }} align="center" gap={4}>
              {loading && (
                <Box w="full">
                  <Progress.Root size="lg" value={null}>
                    <Progress.Label>Loading...</Progress.Label>
                    <Progress.Track>
                      <Progress.Range />
                    </Progress.Track>
                  </Progress.Root>
                </Box>
              )}
              <Image
                ml={{ md: "auto" }}
                alignSelf={{ base: "flex-end", md: "auto" }}
                width="169px"
                height="31px"
                src="/images/powered-by-strava1.png"
                alt="Powered by Strava"
              />
            </Flex>

            <ActivityTypeSelector
              activityTypes={activityTypes}
              selectedActivityType={selectedActivityType}
              onChange={handleActivityTypeChange}
            />

            <ProfileOverview
              athleteProfile={athleteProfile}
              profileLoading={profileLoading}
              selectedActivityType={selectedActivityType}
              units={profileUnits}
              refreshLoading={refreshLoading}
              logoutLoading={logoutLoading}
              searchQuery={searchQuery}
              searchSuggestions={searchSuggestions}
              suggestionsLoading={suggestionsLoading}
              onRefresh={handleRefresh}
              onUnitsChange={handleUnitsChange}
              onLogout={handleLogout}
              onSearchInputChange={handleSearchInputChange}
              onSuggestionSelect={handleSuggestionSelect}
              onSearch={handleSearch}
              onSearchKeyPress={handleSearchKeyPress}
            />

            <LastActivityRoute3D units={profileUnits} selectedActivityType={selectedActivityType} />

            <AnalyticsSection
              selectedActivityType={selectedActivityType}
              athleteProfile={athleteProfile}
              units={profileUnits}
            />
          </>
        )}
      </VStack>

      <WindyMap />
    </Box>
  );
};

export default Home;
