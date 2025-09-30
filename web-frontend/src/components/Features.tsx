import { Box, Container, VStack, HStack, Text, Grid, GridItem, Button } from '@chakra-ui/react';
import { BarChart3, Calendar, Map, Zap, Target, Users } from 'lucide-react';

export default function Features() {
  const features = [
    {
      icon: BarChart3,
      title: 'Performance Analytics',
      description: 'Track your progress with detailed statistics, charts, and insights into your cycling performance over time.',
      gradientFrom: 'blue.500',
      gradientTo: 'cyan.500',
    },
    {
      icon: Calendar,
      title: 'Activity Heatmaps',
      description: 'Visualize your training consistency with beautiful daily distance heatmaps and activity patterns.',
      gradientFrom: 'cyan.500',
      gradientTo: 'teal.500',
    },
    {
      icon: Map,
      title: 'Elevation Tracking',
      description: 'Monitor your climbing achievements with yearly elevation tracking and distribution analysis.',
      gradientFrom: 'teal.500',
      gradientTo: 'emerald.500',
    },
    {
      icon: Zap,
      title: 'Real-time Sync',
      description: 'Seamlessly sync with Strava to keep your stats up-to-date automatically with every ride.',
      gradientFrom: 'emerald.500',
      gradientTo: 'green.500',
    },
    {
      icon: Target,
      title: 'Goal Setting',
      description: 'Set personal goals and track your progress towards achieving new cycling milestones.',
      gradientFrom: 'green.500',
      gradientTo: 'lime.500',
    },
    {
      icon: Users,
      title: 'Community Insights',
      description: 'Compare your performance with the community and discover new challenges to conquer.',
      gradientFrom: 'lime.500',
      gradientTo: 'yellow.500',
    },
  ];

  return (
    <Box as="section" id="features" py={24} bgGradient="to-b" gradientFrom="white" gradientTo="gray.50">
      <Container maxW="container.xl">
        <VStack textAlign="center" maxW="3xl" mx="auto" mb={16} gap={6}>
          <HStack
            gap={2}
            px={4}
            py={2}
            bg="blue.50"
            border="1px"
            borderColor="blue.200"
            borderRadius="full"
            color="blue.700"
            fontSize="sm"
            fontWeight="medium"
          >
            <Text>Features</Text>
          </HStack>
          <VStack gap={6}>
            <Text fontSize={{ base: '4xl', lg: '5xl' }} fontWeight="bold" color="gray.900">
              Everything You Need to
              <Text
                as="span"
                display="block"
                bgGradient="to-r"
                gradientFrom="brand.600"
                gradientTo="cyan.600"
                bgClip="text"
              >
                Track Your Progress
              </Text>
            </Text>
            <Text fontSize="lg" color="gray.600">
              Powerful analytics and insights designed specifically for cyclists who want to take their performance to the next level.
            </Text>
          </VStack>
        </VStack>

        <Grid templateColumns={{ base: '1fr', md: 'repeat(2, 1fr)', lg: 'repeat(3, 1fr)' }} gap={8}>
          {features.map((feature, index) => (
            <GridItem key={index}>
              <Box
                position="relative"
                bg="white"
                p={8}
                borderRadius="2xl"
                border="1px"
                borderColor="gray.200"
                _hover={{
                  borderColor: 'blue.300',
                  boxShadow: '2xl',
                }}
                transition="all 0.3s"
              >
                <Box
                  display="inline-flex"
                  p={4}
                  bgGradient="to-br"
                  gradientFrom={feature.gradientFrom}
                  gradientTo={feature.gradientTo}
                  borderRadius="xl"
                  mb={6}
                  boxShadow="lg"
                  _groupHover={{ transform: 'scale(1.1)' }}
                  transition="transform 0.3s"
                >
                  <feature.icon size={28} color="white" strokeWidth={2.5} />
                </Box>

                <Text fontSize="xl" fontWeight="bold" color="gray.900" mb={3}>
                  {feature.title}
                </Text>
                <Text color="gray.600" lineHeight="relaxed">
                  {feature.description}
                </Text>

                <Box
                  position="absolute"
                  inset={0}
                  bgGradient="to-br"
                  gradientFrom="blue.50"
                  gradientTo="transparent"
                  borderRadius="2xl"
                  opacity={0}
                  _groupHover={{ opacity: 1 }}
                  transition="opacity 0.3s"
                  zIndex={-1}
                />
              </Box>
            </GridItem>
          ))}
        </Grid>

        <Box
          mt={20}
          bgGradient="to-r"
          gradientFrom="brand.600"
          gradientTo="cyan.600"
          borderRadius="3xl"
          p={12}
          textAlign="center"
          boxShadow="2xl"
        >
          <Text fontSize={{ base: '3xl', lg: '4xl' }} fontWeight="bold" color="white" mb={4}>
            Ready to Transform Your Training?
          </Text>
          <Text color="blue.100" fontSize="lg" mb={8} maxW="2xl" mx="auto">
            Join thousands of cyclists who are already using VeloCorner to track their progress and achieve their goals.
          </Text>
          <Button
            size="lg"
            bg="white"
            color="blue.600"
            fontWeight="semibold"
            _hover={{
              boxShadow: '2xl',
              transform: 'scale(1.05)',
            }}
            transition="all 0.2s"
          >
            Connect Your Strava Account
          </Button>
        </Box>
      </Container>
    </Box>
  );
}
