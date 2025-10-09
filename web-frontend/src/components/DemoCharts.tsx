import { Box, Container, VStack, HStack, Text, Grid, GridItem, Flex, Badge } from '@chakra-ui/react';
import { LuTrendingUp, LuChartBar, LuCloud } from 'react-icons/lu';

export default function DemoCharts() {
  const yearlyDistance = [
    { year: '2021', distance: 8240, color: 'blue.500' },
    { year: '2022', distance: 12350, color: 'cyan.500' },
    { year: '2023', distance: 15820, color: 'teal.500' },
    { year: '2024', distance: 18432, color: 'emerald.500' },
  ];

  const maxDistance = Math.max(...yearlyDistance.map(d => d.distance));

  const elevationData = [
    { month: 'Jan', elevation: 12500 },
    { month: 'Feb', elevation: 15200 },
    { month: 'Mar', elevation: 18900 },
    { month: 'Apr', elevation: 22400 },
    { month: 'May', elevation: 28100 },
    { month: 'Jun', elevation: 31500 },
    { month: 'Jul', elevation: 35800 },
    { month: 'Aug', elevation: 40200 },
    { month: 'Sep', elevation: 43600 },
    { month: 'Oct', elevation: 46800 },
    { month: 'Nov', elevation: 49200 },
    { month: 'Dec', elevation: 51400 },
  ];

  const maxElevation = Math.max(...elevationData.map(d => d.elevation));

  return (
    <Box as="section" py={24} bg="white">
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
            <Text>Live Demo</Text>
          </HStack>
          <VStack gap={6}>
            <Text fontSize={{ base: '4xl', lg: '5xl' }} fontWeight="bold" color="gray.900">
              Powerful Visualizations
              <Text
                as="span"
                display="block"
                bgGradient="to-r"
                gradientFrom="brand.600"
                gradientTo="cyan.600"
                bgClip="text"
              >
                At Your Fingertips
              </Text>
            </Text>
            <Text fontSize="lg" color="gray.600">
              See your cycling data come to life with interactive charts and insights that help you understand your performance.
            </Text>
          </VStack>
        </VStack>

        <Grid templateColumns={{ base: '1fr', lg: 'repeat(2, 1fr)' }} gap={8} mb={8}>
          <GridItem>
            <Box
              bgGradient="to-br"
              gradientFrom="yellow.50"
              gradientTo="orange.50"
              p={8}
              borderRadius="2xl"
              border="1px"
              borderColor="yellow.200"
              boxShadow="lg"
            >
              <HStack gap={3} mb={6}>
                <Box p={2} bgGradient="to-br" gradientFrom="brand.600" gradientTo="cyan.600" borderRadius="lg">
                  <LuChartBar size={20} color="white" strokeWidth={2.5} />
                </Box>
                <Text fontSize="xl" fontWeight="bold" color="gray.900">
                  YTD Distance
                </Text>
              </HStack>

              <Flex alignItems="flex-end" justifyContent="space-around" gap={4} h={64} pt={4}>
                {yearlyDistance.map((item, index) => (
                  <VStack key={index} gap={2} flex={1}>
                    <Box position="relative" w="full" maxW="80px" display="flex" flexDirection="column" justifyContent="flex-end" h="200px">
                      <Box
                        bg={item.color}
                        borderTopRadius="lg"
                        boxShadow="lg"
                        transition="all 0.5s"
                        _hover={{ boxShadow: 'xl' }}
                        position="relative"
                        h={`${(item.distance / maxDistance) * 100}%`}
                        css={{
                          transformStyle: 'preserve-3d',
                          transform: 'perspective(400px) rotateY(-8deg)',
                        }}
                      >
                        <Box
                          position="absolute"
                          top="-32px"
                          left="50%"
                          transform="translateX(-50%)"
                          bg="gray.900"
                          color="white"
                          px={2}
                          py={1}
                          borderRadius="md"
                          fontSize="xs"
                          fontWeight="semibold"
                          whiteSpace="nowrap"
                          opacity={0}
                          _groupHover={{ opacity: 1 }}
                          transition="opacity 0.2s"
                        >
                          {item.distance.toLocaleString()} km
                        </Box>
                      </Box>
                    </Box>
                    <Text fontSize="sm" fontWeight="semibold" color="gray.700">
                      {item.year}
                    </Text>
                  </VStack>
                ))}
              </Flex>

              <Flex mt={6} pt={4} borderTop="1px" borderColor="yellow.300" alignItems="center" justifyContent="space-between" fontSize="sm">
                <Text color="gray.600">Activity Type</Text>
                <Badge px={3} py={1} bg="white" borderRadius="lg" fontWeight="medium" color="blue.600" border="1px" borderColor="blue.200">
                  Ride
                </Badge>
              </Flex>
            </Box>
          </GridItem>

          <GridItem>
            <Box
              bgGradient="to-br"
              gradientFrom="yellow.50"
              gradientTo="orange.50"
              p={8}
              borderRadius="2xl"
              border="1px"
              borderColor="yellow.200"
              boxShadow="lg"
            >
              <HStack gap={3} mb={6}>
                <Box p={2} bgGradient="to-br" gradientFrom="teal.600" gradientTo="emerald.600" borderRadius="lg">
                  <LuTrendingUp size={20} color="white" strokeWidth={2.5} />
                </Box>
                <Text fontSize="xl" fontWeight="bold" color="gray.900">
                  Yearly Elevation
                </Text>
              </HStack>

              <Box position="relative" h={64} pt={4}>
                <svg width="100%" height="100%" viewBox="0 0 400 200" preserveAspectRatio="none">
                  <defs>
                    <linearGradient id="elevationGradient" x1="0%" y1="0%" x2="0%" y2="100%">
                      <stop offset="0%" stopColor="rgb(20, 184, 166)" stopOpacity="0.3" />
                      <stop offset="100%" stopColor="rgb(20, 184, 166)" stopOpacity="0.05" />
                    </linearGradient>
                  </defs>

                  <path
                    d={`M 0 ${200 - (elevationData[0].elevation / maxElevation) * 180} ${elevationData.map((d, i) => {
                      const x = (i / (elevationData.length - 1)) * 400;
                      const y = 200 - (d.elevation / maxElevation) * 180;
                      return `L ${x} ${y}`;
                    }).join(' ')} L 400 200 L 0 200 Z`}
                    fill="url(#elevationGradient)"
                  />

                  <path
                    d={`M 0 ${200 - (elevationData[0].elevation / maxElevation) * 180} ${elevationData.map((d, i) => {
                      const x = (i / (elevationData.length - 1)) * 400;
                      const y = 200 - (d.elevation / maxElevation) * 180;
                      return `L ${x} ${y}`;
                    }).join(' ')}`}
                    fill="none"
                    stroke="rgb(20, 184, 166)"
                    strokeWidth="3"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  />

                  {elevationData.map((d, i) => {
                    const x = (i / (elevationData.length - 1)) * 400;
                    const y = 200 - (d.elevation / maxElevation) * 180;
                    return (
                      <circle
                        key={i}
                        cx={x}
                        cy={y}
                        r="4"
                        fill="white"
                        stroke="rgb(20, 184, 166)"
                        strokeWidth="2"
                      />
                    );
                  })}
                </svg>

                <Flex position="absolute" bottom={0} left={0} right={0} justifyContent="space-between" px={2} fontSize="xs" color="gray.500" fontWeight="medium">
                  {elevationData.filter((_, i) => i % 3 === 0).map((d, i) => (
                    <Text key={i}>{d.month}</Text>
                  ))}
                </Flex>
              </Box>

              <Flex mt={6} pt={4} borderTop="1px" borderColor="yellow.300" alignItems="center" justifyContent="space-between" fontSize="sm">
                <Text color="gray.600">Total Elevation</Text>
                <Badge px={3} py={1} bg="white" borderRadius="lg" fontWeight="semibold" color="emerald.600" border="1px" borderColor="emerald.200">
                  51,400 m
                </Badge>
              </Flex>
            </Box>
          </GridItem>
        </Grid>

      </Container>
    </Box>
  );
}
