import { Box, Container, Grid, GridItem, VStack, HStack, Text, Link, Flex, Image, Badge } from '@chakra-ui/react';
import instagram from 'super-tiny-icons/images/svg/instagram.svg'
import github from 'super-tiny-icons/images/svg/github.svg'
import strava from 'super-tiny-icons/images/svg/strava.svg'
import youtube from 'super-tiny-icons/images/svg/youtube.svg'
import twitter from 'super-tiny-icons/images/svg/twitter.svg'
import email from 'super-tiny-icons/images/svg/email.svg'
import facebook from 'super-tiny-icons/images/svg/facebook.svg'
import Social from './Social';
import { LuHeart } from 'react-icons/lu';

export default function Footer() {
  return (
    <Box as="footer" id="about" pt={12} pb={16}>
      <Container maxW="container.xl">
        <Box
          px={{ base: 6, md: 8 }}
          py={{ base: 8, md: 10 }}
          border="1px solid"
          borderColor="rgba(20, 32, 51, 0.08)"
          bg="rgba(17, 24, 39, 0.94)"
          color="white"
          borderRadius="32px"
          boxShadow="0 24px 60px rgba(11, 18, 31, 0.24)"
        >
          <Grid templateColumns={{ base: '1fr', md: '1.7fr 1fr 1fr' }} gap={10} mb={10}>
            <GridItem>
              <Badge colorPalette="green" borderRadius="full" px={3} py={1} mb={4}>
                Built for riding more intentionally
              </Badge>
              <HStack gap={3} mb={4}>
              <Image boxSize="40px" src='/images/logo50-gray.png' alt="Velocorner Logo" />
              <Text fontSize="xl" fontWeight="bold" color="white" textTransform="uppercase">
                Velocorner
              </Text>
            </HStack>
              <Text color="gray.300" mb={6} maxW="lg" lineHeight="1.8">
                Concise endurance analytics for athletes who want fewer dashboards, clearer signals, and faster answers from their Strava data.
              </Text>
              <HStack gap={4} flexWrap="wrap">
                <Social href="https://www.instagram.com/peregin" image={instagram} alt="Instagram" />
                <Social href="https://github.com/velocorner" image={github} alt="GitHub" />
                <Social href="https://www.strava.com/clubs/velocorner" image={strava} alt="Strava" />
                <Social href="https://www.youtube.com/channel/UCekBQAfLHviXTvB1kVjzBZQ" image={youtube} alt="YouTube" />
                <Social href="https://twitter.com/velocorner_com" image={twitter} alt="Twitter" />
                <Social href="mailto:velocorner.com@gmail.com" image={email} alt="Email" />
                <Social href="https://www.facebook.com/101313662146829" image={facebook} alt="Facebook" />
              </HStack>
            </GridItem>

            <GridItem>
              <Text color="white" fontWeight="semibold" mb={4}>
                Explore
              </Text>
              <VStack alignItems="flex-start" gap={3}>
                <Link href="/#stats" color="gray.400" _hover={{ color: 'white' }} transition="colors 0.2s">
                  Dashboard
                </Link>
                <Link href="/search" color="gray.400" _hover={{ color: 'white' }} transition="colors 0.2s">
                  Activity search
                </Link>
                <Link href="/about" color="gray.400" _hover={{ color: 'white' }} transition="colors 0.2s">
                  About
                </Link>
                <Link href="/privacy" color="gray.400" _hover={{ color: 'white' }} transition="colors 0.2s">
                  Privacy
                </Link>
              </VStack>
            </GridItem>

            <GridItem>
              <Text color="white" fontWeight="semibold" mb={4}>
                Resources
              </Text>
              <VStack alignItems="flex-start" gap={3}>
                <Link href="/docs" color="gray.400" _hover={{ color: 'white' }} transition="colors 0.2s">
                  API docs
                </Link>
                <Link href="https://github.com/velocorner" color="gray.400" _hover={{ color: 'white' }} transition="colors 0.2s">
                  Source code
                </Link>
                <Link href="mailto:velocorner.com@gmail.com" color="gray.400" _hover={{ color: 'white' }} transition="colors 0.2s">
                  Contact
                </Link>
              </VStack>
            </GridItem>
          </Grid>

          <Box borderTop="1px" borderColor="whiteAlpha.200" pt={8}>
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
        </Box>
      </Container>
    </Box>
  );
}
