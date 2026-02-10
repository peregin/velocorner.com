import ApiClient from '@/service/ApiClient';
import { UserProgress, UserStats } from '@/types/athlete';
import { Box, HStack, Text, Icon, VStack } from '@chakra-ui/react';
import { useEffect, useState } from 'react';
import { LuActivity, LuTrendingUp, LuMountain, LuCalendar } from 'react-icons/lu';

interface QuickStatsProps {
  selectedActivityType: string;
}

const QuickStats = ({ selectedActivityType }: QuickStatsProps) => {

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
      label: 'Total Activities',
      gradientFrom: 'blue.500',
      gradientTo: 'cyan.500',
    },
    {
      icon: LuTrendingUp,
      value: userStats?.progress?.distance ?? 0,
      label: 'Distance',
      gradientFrom: 'cyan.800',
      gradientTo: 'teal.500',
    },
    {
      icon: LuMountain,
      value: userStats?.progress?.elevation ?? 0,
      label: 'Elevation',
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

  return (
    <HStack gap={6} align="stretch" flexWrap="wrap" width="100%">
      {stats.map((stat, index) => (
        <Box
          key={index}
          p={4}
          bgGradient="to-br"
          gradientFrom="gray.50"
          gradientTo="white"
          borderRadius="2xl"
          border="1px"
          borderColor="gray.200"
          _hover={{
            borderColor: 'blue.300',
            boxShadow: 'xl',
          }}
          transition="all 0.3s"
          flex="1"
          // minW="120px"
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
              {stat.value}
            </Text>
            <Text fontSize="sm" fontWeight="medium" color="gray.600">
              {stat.label}
            </Text>
          </VStack>
        </Box>
      ))}
    </HStack>
  );
};

export default QuickStats;