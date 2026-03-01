import { useEffect, useState } from "react";
import ApiClient from "./ApiClient";
import { toaster } from "@/components/ui/toaster";
import type { AthleteProfile } from "@/types/athlete";

const showError = (title: string, description: string) => {
  toaster.create({ title, description, type: "error", duration: 5000 });
};

export const useAthleteProfile = (isAuthenticated: boolean, withErrorToast = false) => {
  const [athleteProfile, setAthleteProfile] = useState<AthleteProfile | null>(null);
  const [profileLoading, setProfileLoading] = useState(false);

  useEffect(() => {
    let isActive = true;

    if (!isAuthenticated) {
      setAthleteProfile(null);
      setProfileLoading(false);
      return () => {
        isActive = false;
      };
    }

    const fetchAthleteProfile = async () => {
      try {
        setProfileLoading(true);
        const profile = await ApiClient.athleteProfile();
        if (isActive) {
          setAthleteProfile(profile);
        }
      } catch (error) {
        console.error("Error fetching athlete profile:", error);
        if (isActive && withErrorToast) {
          showError("Unable to load profile", "We couldn't load your profile information right now.");
        }
      } finally {
        if (isActive) {
          setProfileLoading(false);
        }
      }
    };

    fetchAthleteProfile();

    return () => {
      isActive = false;
    };
  }, [isAuthenticated, withErrorToast]);

  return { athleteProfile, profileLoading, setAthleteProfile };
};
