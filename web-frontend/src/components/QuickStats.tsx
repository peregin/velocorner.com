import ApiClient from '@/service/ApiClient';
import type { AthleteUnits, UserStats } from '@/types/athlete';
import { Box, Grid, Text, Icon, VStack } from '@chakra-ui/react';
import { useEffect, useState } from 'react';
import { LuActivity, LuTrendingUp, LuMountain, LuCalendar } from 'react-icons/lu';

interface QuickStatsProps {
  selectedActivityType: string;
  units: AthleteUnits;
}

const QuickStats = ({ selectedActivityType, units }: QuickStatsProps) => {

  const [userStats, setUserStats] = useState<UserStats | null>(null);
  const currentYear = new Date().getFullYear();

  useEffect(() => {
    const fetchUserData = async () => {
      try {
        const stats = await ApiClient.profileStatistics(selectedActivityType, currentYear);
        setUserStats(stats);
      } catch (error) {
        console.error('Error fetching user stats:', error);
      }
    };

    fetchUserData();
  }, [selectedActivityType]);

  const stats = [
    {
      icon: LuActivity,
      value: userStats?.progress?.rides ?? 0,
      label: 'Activities',
      gradientFrom: 'blue.500',
      gradientTo: 'cyan.500',
    },
    {
      icon: LuTrendingUp,
      value: userStats?.progress?.distance ?? 0,
      label: 'Distance',
      unit: units.distanceLabel,
      gradientFrom: 'cyan.800',
      gradientTo: 'teal.500',
    },
    {
      icon: LuMountain,
      value: userStats?.progress?.elevation ?? 0,
      label: 'Elevation',
      unit: units.elevationLabel,
      gradientFrom: 'teal.500',
      gradientTo: 'emerald.500',
    },
    {
      icon: LuCalendar,
      value: userStats?.progress?.days ?? 0,
      label: 'Active Days',
      gradientFrom: 'red.500',
      gradientTo: 'orange.500',
    },
  ];

  const formatWholeNumber = (value: number) => Math.round(value).toLocaleString();

  return (
    <Grid
      gap={{ base: 3, md: 4 }}
      width="100%"
      templateColumns={{ base: '1fr 1fr', xl: 'repeat(4, 1fr)' }}
    >
      {stats.map((stat, index) => (
        <Box
          key={index}
          p={{ base: 3, md: 4 }}
          bgGradient="to-br"
          gradientFrom="white"
          gradientTo="gray.50"
          borderRadius="2xl"
          border="1px"
          borderColor="gray.100"
          _hover={{
            borderColor: 'teal.200',
            boxShadow: 'lg',
            transform: 'translateY(-2px)'
          }}
          transition="all 0.3s"
        >
          <Box
            display="inline-flex"
            p={2}
            bgGradient="to-br"
            gradientFrom={stat.gradientFrom}
            gradientTo={stat.gradientTo}
            borderRadius="xl"
            mb={2}
            boxShadow="lg"
          >
            <Icon as={stat.icon} fontSize="20px" color="gray" strokeWidth={2.5} />
          </Box>

          <VStack alignItems="flex-start" gap={1} w="full">
            <Text fontSize="2xl" fontWeight="bold" color="gray.900">
              {formatWholeNumber(stat.value)}
              {stat.unit && (
                <Text as="span" fontSize="sm" fontWeight="medium" color="gray.600" ml={2}>
                  {stat.unit}
                </Text>
              )}
            </Text>
            <Text fontSize="sm" fontWeight="medium" color="gray.600">
              {stat.label}
            </Text>
          </VStack>
        </Box>
      ))}
    </Grid>
  );
};

export default QuickStats;
