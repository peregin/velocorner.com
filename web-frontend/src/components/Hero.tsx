import { Box, Container, Flex, HStack, VStack, Text, Button, Icon, Link, Image } from '@chakra-ui/react';
import { LuGithub, LuTwitter, LuMail } from 'react-icons/lu';

export default function Hero() {
  return (
    <Box as="header" position="relative" overflow="hidden">
      <Container maxW="container.xl" py={{ base: 20, lg: 32 }}>
        
        <Image width='169px' height='31px' src='/images/powered-by-strava1.png' mx="auto" display="block" />
        <VStack maxW="4xl" mx="auto" textAlign="center" gap={6}>
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
