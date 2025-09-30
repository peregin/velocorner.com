import { Box, Container, Grid, GridItem, VStack, HStack, Text, Link, Flex } from '@chakra-ui/react';
import { TrendingUp, Github, Twitter, Mail, Heart } from 'lucide-react';

export default function Footer2() {
  return (
    <Box as="footer" id="about" bg="gray.900" color="gray.300" py={16}>
      <Container maxW="container.xl">
        <Grid templateColumns={{ base: '1fr', md: 'repeat(4, 1fr)' }} gap={12} mb={12}>
          <GridItem colSpan={{ base: 1, md: 2 }}>
            <HStack gap={3} mb={4}>
              <Box
                bgGradient="to-br"
                gradientFrom="brand.600"
                gradientTo="cyan.500"
                p={2.5}
                borderRadius="xl"
                boxShadow="lg"
              >
                <TrendingUp size={24} color="white" strokeWidth={2.5} />
              </Box>
              <Text fontSize="xl" fontWeight="bold" color="white">
                VeloCorner
              </Text>
            </HStack>
            <Text color="gray.400" mb={6} maxW="md" lineHeight="relaxed">
              Your personal cycling analytics platform. Track your performance, visualize your progress,
              and celebrate every milestone with powerful insights from your Strava data.
            </Text>
            <HStack gap={4}>
              <Link
                href="https://github.com/velocorner"
                target="_blank"
                rel="noopener noreferrer"
                p={3}
                bg="gray.800"
                borderRadius="lg"
                color="gray.400"
                _hover={{ color: 'white', bg: 'gray.700' }}
                transition="all 0.2s"
              >
                <Github size={20} />
              </Link>
              <Link
                href="https://twitter.com/velocorner"
                target="_blank"
                rel="noopener noreferrer"
                p={3}
                bg="gray.800"
                borderRadius="lg"
                color="gray.400"
                _hover={{ color: 'white', bg: 'gray.700' }}
                transition="all 0.2s"
              >
                <Twitter size={20} />
              </Link>
              <Link
                href="mailto:contact@velocorner.com"
                p={3}
                bg="gray.800"
                borderRadius="lg"
                color="gray.400"
                _hover={{ color: 'white', bg: 'gray.700' }}
                transition="all 0.2s"
              >
                <Mail size={20} />
              </Link>
            </HStack>
          </GridItem>

          <GridItem>
            <Text color="white" fontWeight="semibold" mb={4}>
              Product
            </Text>
            <VStack alignItems="flex-start" gap={3}>
              <Link href="#features" color="gray.400" _hover={{ color: 'white' }} transition="colors 0.2s">
                Features
              </Link>
              <Link href="#stats" color="gray.400" _hover={{ color: 'white' }} transition="colors 0.2s">
                Statistics
              </Link>
              <Link href="#" color="gray.400" _hover={{ color: 'white' }} transition="colors 0.2s">
                Pricing
              </Link>
              <Link href="#" color="gray.400" _hover={{ color: 'white' }} transition="colors 0.2s">
                Demo
              </Link>
            </VStack>
          </GridItem>

          <GridItem>
            <Text color="white" fontWeight="semibold" mb={4}>
              Resources
            </Text>
            <VStack alignItems="flex-start" gap={3}>
              <Link href="#" color="gray.400" _hover={{ color: 'white' }} transition="colors 0.2s">
                Documentation
              </Link>
              <Link href="#" color="gray.400" _hover={{ color: 'white' }} transition="colors 0.2s">
                API
              </Link>
              <Link href="#" color="gray.400" _hover={{ color: 'white' }} transition="colors 0.2s">
                Support
              </Link>
              <Link href="#" color="gray.400" _hover={{ color: 'white' }} transition="colors 0.2s">
                Blog
              </Link>
            </VStack>
          </GridItem>
        </Grid>

        <Box borderTop="1px" borderColor="gray.800" pt={8}>
          <Flex
            flexDirection={{ base: 'column', md: 'row' }}
            alignItems="center"
            justifyContent="space-between"
            gap={4}
          >
            <Text color="gray.400" fontSize="sm">
              © 2025 VeloCorner. All rights reserved.
            </Text>
            <HStack gap={2} fontSize="sm" color="gray.400">
              <Text>Made with</Text>
              <Heart size={16} color="#ef4444" fill="#ef4444" />
              <Text>for cyclists</Text>
            </HStack>
          </Flex>
        </Box>
      </Container>
    </Box>
  );
}
