import { Box, Container, Grid, GridItem, VStack, HStack, Text, Link, Flex, Image } from '@chakra-ui/react';
import instagram from 'super-tiny-icons/images/svg/instagram.svg'
import github from 'super-tiny-icons/images/svg/github.svg'
import strava from 'super-tiny-icons/images/svg/strava.svg'
import youtube from 'super-tiny-icons/images/svg/youtube.svg'
import twitter from 'super-tiny-icons/images/svg/twitter.svg'
import email from 'super-tiny-icons/images/svg/email.svg'
import facebook from 'super-tiny-icons/images/svg/facebook.svg'
import Social from './Social';
import { LuHeart, LuTrendingUp } from 'react-icons/lu';

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
                <LuTrendingUp size={24} color="white" strokeWidth={2.5} />
              </Box>
              <Text fontSize="xl" fontWeight="bold" color="white" textTransform="uppercase">
                Velocorner
              </Text>
            </HStack>
            <Text color="gray.400" mb={6} maxW="md" lineHeight="relaxed">
              Your personal cycling analytics platform. Track your performance, visualize your progress,
              and celebrate every milestone with powerful insights from your Strava data.
            </Text>
            <HStack gap={4}>
              <Social href="https://www.instagram.com/peregin" image={instagram} alt="Instagram" />
              <Social href="https://github.com/velocorner" image={github} alt="GitHub" />
              <Social href="https://www.strava.com/clubs/velocorner" image={strava} alt="Strava" />
              <Social href="https://www.youtube.com/channel/UCekBQAfLHviXTvB1kVjzBZQ" image={youtube} alt="YouTube" />
              <Social href="https://twitter.com/velocorner_com" image={twitter} alt="Twitter" />
              <Social href="mailto:velocorner.com@@gmail.com" image={email} alt="Email" />
              <Social href="https://www.facebook.com/101313662146829" image={facebook} alt="Facebook" />
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
              © 2015 - {new Date().getFullYear()} Velocorner. All rights reserved.
            </Text>
            <HStack gap={2} fontSize="sm" color="gray.400">
              <Text>Made with</Text>
              <LuHeart size={16} color="#ef4444" fill="#ef4444" />
              <Text>for cyclists</Text>
            </HStack>
          </Flex>
        </Box>
        {/* add status page widget that shows the status of the site */}
      </Container>
    </Box>
  );
}
