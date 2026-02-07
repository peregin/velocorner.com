import { Box, HStack, Text, Icon, Progress, VStack } from '@chakra-ui/react';
import { LuActivity, LuTrendingUp, LuMountain } from 'react-icons/lu';

interface QuickStatsProps {
  athleteProfile: any;
  selectedActivityType: string;
}

const QuickStats = ({ athleteProfile, selectedActivityType }: QuickStatsProps) => {
  const stats = [
    {
      icon: LuActivity,
      value: athleteProfile?.totalActivities || 0,
      label: 'Total Activities',
      gradientFrom: 'blue.500',
      gradientTo: 'cyan.500',
    },
    {
      icon: LuTrendingUp,
      value: athleteProfile?.ytdDistance || 0,
      label: 'Yearly Distance',
      gradientFrom: 'cyan.800',
      gradientTo: 'teal.500',
    },
    {
      icon: LuMountain,
      value: athleteProfile?.ytdElevation || 0,
      label: 'Yearly Elevation',
      gradientFrom: 'teal.500',
      gradientTo: 'emerald.500',
    },
  ];

  return (
    <HStack gap={6} align="stretch" flexWrap="wrap">
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
          minW="120px"
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