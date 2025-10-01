import { Box, Container, Grid, GridItem, VStack, HStack, Text, Icon } from '@chakra-ui/react';
import { LuActivity, LuTrendingUp, LuMountain, LuCalendar } from 'react-icons/lu';

export default function Stats() {
  const stats = [
    {
      icon: LuActivity,
      value: '2,547',
      label: 'Total Activities',
      change: '+12.5%',
      gradientFrom: 'blue.500',
      gradientTo: 'cyan.500',
    },
    {
      icon: LuTrendingUp,
      value: '18,432 km',
      label: 'Distance This Year',
      change: '+8.2%',
      gradientFrom: 'cyan.500',
      gradientTo: 'teal.500',
    },
    {
      icon: LuMountain,
      value: '234,891 m',
      label: 'Elevation Gained',
      change: '+15.7%',
      gradientFrom: 'teal.500',
      gradientTo: 'emerald.500',
    },
    {
      icon: LuCalendar,
      value: '186 days',
      label: 'Active Days',
      change: '+4.3%',
      gradientFrom: 'emerald.500',
      gradientTo: 'green.500',
    },
  ];

  return (
    <Box as="section" id="stats" py={16} bg="white">
      <Container maxW="container.xl">
        <Grid templateColumns={{ base: '1fr', sm: 'repeat(2, 1fr)', lg: 'repeat(4, 1fr)' }} gap={6}>
          {stats.map((stat, index) => (
            <GridItem key={index}>
              <Box
                position="relative"
                bgGradient="to-br"
                gradientFrom="gray.50"
                gradientTo="white"
                p={8}
                borderRadius="2xl"
                border="1px"
                borderColor="gray.200"
                _hover={{
                  borderColor: 'blue.300',
                  boxShadow: 'xl',
                }}
                transition="all 0.3s"
              >
                <Box
                  display="inline-flex"
                  p={3}
                  bgGradient="to-br"
                  gradientFrom={stat.gradientFrom}
                  gradientTo={stat.gradientTo}
                  borderRadius="xl"
                  mb={4}
                  boxShadow="lg"
                >
                  <Icon fontSize="24px" color="white">
                    <stat.icon strokeWidth={2.5} />
                  </Icon>
                </Box>

                <VStack alignItems="flex-start" gap={1}>
                  <Text fontSize="3xl" fontWeight="bold" color="gray.900">
                    {stat.value}
                  </Text>
                  <Text fontSize="sm" fontWeight="medium" color="gray.600">
                    {stat.label}
                  </Text>
                </VStack>

                <HStack mt={4} gap={1} fontSize="sm" fontWeight="medium" color="green.600">
                  <Icon fontSize="16px">
                    <LuTrendingUp />
                  </Icon>
                  <Text>{stat.change}</Text>
                </HStack>

                <Box
                  position="absolute"
                  top={0}
                  right={0}
                  w={32}
                  h={32}
                  bgGradient="to-br"
                  gradientFrom="blue.100"
                  gradientTo="transparent"
                  borderRadius="full"
                  opacity={0}
                  _groupHover={{ opacity: 0.2 }}
                  transition="opacity 0.3s"
                  filter="blur(40px)"
                />
              </Box>
            </GridItem>
          ))}
        </Grid>
      </Container>
    </Box>
  );
}
