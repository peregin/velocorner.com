import { useEffect, useState } from "react";
import { Box, Button, Card, Grid, Heading, Progress, Text, VStack, Image } from "@chakra-ui/react";
import strava from "super-tiny-icons/images/svg/strava.svg";
import { useAuth } from "@/service/auth";
import { useAthleteProfile } from "@/service/useAthleteProfile";
import ApiClient from "@/service/ApiClient";
import { isAthleteAdmin } from "@/types/athlete";
import { toaster } from "@/components/ui/toaster";

type AdminStatus = {
  accounts?: number;
  activeAccounts?: number;
  activities?: number;
  markets?: number;
  brands?: number;
};

const showError = (title: string, description: string) => {
  toaster.create({ title, description, type: "error", duration: 5000 });
};

const StatCard = ({ label, value }: { label: string; value?: number }) => (
  <Card.Root>
    <Card.Body>
      <VStack align="start" gap={2}>
        <Text color="gray.500" fontWeight="medium">{label}</Text>
        <Heading size="2xl">{value ?? 0}</Heading>
      </VStack>
    </Card.Body>
  </Card.Root>
);

const Admin = () => {
  const { isAuthenticated, authLoading, connect } = useAuth();
  const { athleteProfile, profileLoading } = useAthleteProfile(isAuthenticated);
  const [adminStatus, setAdminStatus] = useState<AdminStatus | null>(null);
  const [statusLoading, setStatusLoading] = useState(false);

  const isAdmin = isAthleteAdmin(athleteProfile);

  useEffect(() => {
    let isActive = true;

    if (!isAuthenticated || !isAdmin) {
      setAdminStatus(null);
      setStatusLoading(false);
      return () => {
        isActive = false;
      };
    }

    const fetchAdminStatus = async () => {
      try {
        setStatusLoading(true);
        const status = await ApiClient.adminStatus();
        if (isActive) {
          setAdminStatus(status);
        }
      } catch (error) {
        console.error("Error fetching admin status:", error);
        if (isActive) {
          showError("Unable to load admin status", "Please try again in a moment.");
        }
      } finally {
        if (isActive) {
          setStatusLoading(false);
        }
      }
    };

    fetchAdminStatus();

    return () => {
      isActive = false;
    };
  }, [isAuthenticated, isAdmin]);

  if (authLoading || profileLoading) {
    return (
      <Box maxW="1200px" mx="auto" p={6}>
        <Progress.Root size="lg" value={null}>
          <Progress.Track>
            <Progress.Range />
          </Progress.Track>
        </Progress.Root>
        <Text mt={4}>Loading...</Text>
      </Box>
    );
  }

  if (!isAuthenticated) {
    return (
      <Box maxW="1200px" mx="auto" p={6}>
        <Card.Root>
          <Card.Body>
            <VStack gap={4}>
              <Heading size="lg">Admin</Heading>
              <Text>Please connect your Strava account to continue.</Text>
              <Button colorPalette="orange" onClick={connect}>
                <Image src={strava} boxSize="20px" mr={2} />
                Connect with Strava
              </Button>
            </VStack>
          </Card.Body>
        </Card.Root>
      </Box>
    );
  }

  if (!isAdmin) {
    return (
      <Box maxW="1200px" mx="auto" p={6}>
        <Card.Root>
          <Card.Body>
            <VStack align="start" gap={2}>
              <Heading size="lg">Admin</Heading>
              <Text color="gray.600">You do not have access to this page.</Text>
            </VStack>
          </Card.Body>
        </Card.Root>
      </Box>
    );
  }

  return (
    <Box maxW="1200px" mx="auto" p={6}>
      <VStack align="stretch" gap={6}>
        <Heading size="2xl">Admin</Heading>
        {statusLoading ? (
          <Progress.Root size="lg" value={null}>
            <Progress.Track>
              <Progress.Range />
            </Progress.Track>
          </Progress.Root>
        ) : (
          <Grid templateColumns={{ base: "1fr", md: "repeat(3, 1fr)" }} gap={4}>
            <StatCard label="Accounts" value={adminStatus?.accounts} />
            <StatCard label="Active Accounts" value={adminStatus?.activeAccounts} />
            <StatCard label="Activities" value={adminStatus?.activities} />
          </Grid>
        )}

        <Heading size="xl">Products</Heading>
        {statusLoading ? (
          <Progress.Root size="lg" value={null}>
            <Progress.Track>
              <Progress.Range />
            </Progress.Track>
          </Progress.Root>
        ) : (
          <Grid templateColumns={{ base: "1fr", md: "repeat(2, 1fr)" }} gap={4}>
            <StatCard label="Markets" value={adminStatus?.markets} />
            <StatCard label="Brands" value={adminStatus?.brands} />
          </Grid>
        )}
      </VStack>
    </Box>
  );
};

export default Admin;
