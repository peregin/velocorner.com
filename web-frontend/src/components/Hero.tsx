import { Box, Container, Flex, HStack, VStack, Text, Button, Icon, Link, IconButton, Image } from '@chakra-ui/react';
import { LuMenu, LuX, LuTrendingUp, LuGithub, LuTwitter, LuMail } from 'react-icons/lu';

interface HeroProps {
  isMenuOpen: boolean;
  setIsMenuOpen: (value: boolean) => void;
}

export default function Hero({ isMenuOpen, setIsMenuOpen }: HeroProps) {
  return (
    <Box as="header" position="relative" overflow="hidden">
      <Box as="nav" py={6}>
        <Container maxW="container.xl">
          <Flex alignItems="center" justifyContent="space-between">
            <HStack gap={4}>
              <Image boxSize="40px" src='/images/logo50.png' alt="Velocorner Logo"/>
              <Text fontSize='1.5em' fontWeight='semibold' textTransform='uppercase'>Velocorner</Text>
            </HStack>

            <HStack gap={8} display={{ base: 'none', md: 'flex' }}>
              <Link href="#features" color="gray.700" _hover={{ color: 'brand.600' }} fontWeight="medium">
                Features
              </Link>
              <Link href="#stats" color="gray.700" _hover={{ color: 'brand.600' }} fontWeight="medium">
                Statistics
              </Link>
              <Link href="#about" color="gray.700" _hover={{ color: 'brand.600' }} fontWeight="medium">
                About
              </Link>
              <Button
                //bgGradient="to-r"
                gradientFrom="brand.600"
                gradientTo="cyan.600"
                //color="white"
                fontWeight="medium"
                _hover={{ boxShadow: 'lg', transform: 'scale(1.05)' }}
                transition="all 0.2s"
              >
                Connect Strava
              </Button>
            </HStack>

            <IconButton
              display={{ base: 'flex', md: 'none' }}
              onClick={() => setIsMenuOpen(!isMenuOpen)}
              aria-label="Toggle menu"
              variant="ghost"
              color="gray.700"
              _hover={{ color: 'brand.600' }}
            >
              {isMenuOpen ? <LuX /> : <LuMenu />}
            </IconButton>
          </Flex>

          {isMenuOpen && (
            <VStack
              display={{ base: 'flex', md: 'none' }}
              mt={6}
              pb={6}
              gap={4}
              borderTop="1px"
              borderColor="gray.200"
              pt={6}
              alignItems="stretch"
            >
              <Link href="#features" color="gray.700" _hover={{ color: 'brand.600' }} fontWeight="medium">
                Features
              </Link>
              <Link href="#stats" color="gray.700" _hover={{ color: 'brand.600' }} fontWeight="medium">
                Statistics
              </Link>
              <Link href="#about" color="gray.700" _hover={{ color: 'brand.600' }} fontWeight="medium">
                About
              </Link>
              <Button
                bgGradient="to-r"
                gradientFrom="brand.600"
                gradientTo="cyan.600"
                color="gray.700"
                fontWeight="medium"
              >
                Connect Strava
              </Button>
            </VStack>
          )}
        </Container>
      </Box>

      <Container maxW="container.xl" py={{ base: 20, lg: 32 }}>
        <VStack maxW="4xl" mx="auto" textAlign="center" gap={6}>
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
            <Box w={2} h={2} bg="blue.500" borderRadius="full" animation="pulse 2s infinite" />
            <Text>Powered by Strava</Text>
          </HStack>

          <VStack gap={6}>
            <Box>
              <Text fontSize={{ base: '5xl', lg: '7xl' }} fontWeight="bold" color="gray.900" lineHeight="tight">
                Your Cycling Journey,
              </Text>
              <Text
                fontSize={{ base: '5xl', lg: '7xl' }}
                fontWeight="bold"
                bgGradient="to-r"
                gradientFrom="brand.600"
                gradientTo="cyan.600"
                //bgClip="text"
                lineHeight="tight"
              >
                Visualized Beautifully
              </Text>
            </Box>

            <Text fontSize="xl" color="gray.600" maxW="2xl" lineHeight="relaxed">
              Unlock powerful insights from your Strava data. Track your progress, analyze your performance,
              and celebrate every milestone with stunning visualizations.
            </Text>
          </VStack>

          <Flex flexDirection={{ base: 'column', sm: 'row' }} gap={4} justifyContent="center">
            <Button
              size="lg"
              // bgGradient="to-r"
              // gradientFrom="brand.600"
              // gradientTo="cyan.600"
              color="white"
              fontWeight="semibold"
              _hover={{ boxShadow: '2xl', transform: 'scale(1.05)' }}
              transition="all 0.2s"
            >
              <HStack>
                <Text>Get Started Free</Text>
                <Icon>
                  <LuTrendingUp size={20} />
                </Icon>
              </HStack>
            </Button>
            <Button
              size="lg"
              bg="white"
              color="gray.700"
              fontWeight="semibold"
              border="2px"
              borderColor="gray.200"
              _hover={{ borderColor: 'blue.300', boxShadow: 'lg' }}
              transition="all 0.2s"
            >
              View Demo
            </Button>
          </Flex>

          <HStack mt={6} gap={6}>
            <Link
              href="https://github.com/velocorner"
              target="_blank"
              rel="noopener noreferrer"
              p={3}
              bg="white"
              borderRadius="lg"
              border="1px"
              borderColor="gray.200"
              color="gray.600"
              _hover={{ color: 'brand.600', borderColor: 'blue.300', boxShadow: 'md' }}
              transition="all 0.2s"
            >
              <LuGithub size={20} />
            </Link>
            <Link
              href="https://twitter.com/velocorner"
              target="_blank"
              rel="noopener noreferrer"
              p={3}
              bg="white"
              borderRadius="lg"
              border="1px"
              borderColor="gray.200"
              color="gray.600"
              _hover={{ color: 'brand.600', borderColor: 'blue.300', boxShadow: 'md' }}
              transition="all 0.2s"
            >
              <LuTwitter size={20} />
            </Link>
            <Link
              href="mailto:contact@velocorner.com"
              p={3}
              bg="white"
              borderRadius="lg"
              border="1px"
              borderColor="gray.200"
              color="gray.600"
              _hover={{ color: 'brand.600', borderColor: 'blue.300', boxShadow: 'md' }}
              transition="all 0.2s"
            >
              <LuMail size={20} />
            </Link>
          </HStack>
        </VStack>
      </Container>

      <Box
        position="absolute"
        top={0}
        right={0}
        zIndex={-1}
        w="96"
        h="96"
        bg="blue.200"
        borderRadius="full"
        mixBlendMode="multiply"
        filter="blur(60px)"
        opacity={0.3}
        animation="blob 7s infinite"
      />
      <Box
        position="absolute"
        bottom={0}
        left={0}
        zIndex={-1}
        w="96"
        h="96"
        bg="cyan.200"
        borderRadius="full"
        mixBlendMode="multiply"
        filter="blur(60px)"
        opacity={0.3}
        animation="blob 7s infinite"
        css={{ animationDelay: '2s' }}
      />
    </Box>
  );
}
