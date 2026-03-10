import { useAuth } from '@/service/auth';
import { VStack, Flex, Box, Container, HStack, Image, IconButton, Text, Button, Link } from '@chakra-ui/react'
import { LuMenu, LuX } from 'react-icons/lu';
import strava from 'super-tiny-icons/images/svg/strava.svg'
import { useAthleteProfile } from '@/service/useAthleteProfile';
import { isAthleteAdmin } from '@/types/athlete';

interface HeaderProps {
  isMenuOpen: boolean;
  setIsMenuOpen: (value: boolean) => void;
}

const Header = ({ isMenuOpen, setIsMenuOpen }: HeaderProps) => {

  const { isAuthenticated, authLoading, connect } = useAuth();
  const { athleteProfile } = useAthleteProfile(isAuthenticated);
  const isAdmin = isAthleteAdmin(athleteProfile);
  const links = [
    { href: "/#stats", label: "Stats" },
    { href: "/search", label: "Search" },
    { href: "/about", label: "About" },
  ];

  return (
    <Box as="nav" position="sticky" top="0" zIndex="40" py={{ base: 4, md: 5 }}>
      <Container maxW="container.xl">
        <Flex alignItems="center" justifyContent="space-between">
          <Flex
            alignItems="center"
            justifyContent="space-between"
            width="100%"
            px={{ base: 4, md: 6 }}
            py={3}
            border="1px solid"
            borderColor="rgba(20, 32, 51, 0.08)"
            bg="rgba(255,255,255,0.78)"
            backdropFilter="blur(18px)"
            borderRadius="full"
            boxShadow="0 20px 40px rgba(20, 32, 51, 0.08)"
          >
            <Link href="/" color="slate.800" _hover={{ color: 'brand.700' }} fontWeight="medium">
              <HStack gap={3}>
              <Image boxSize="40px" src='/images/logo50.png' alt="Velocorner Logo" />
                <VStack align="start" gap={0}>
                  <Text fontSize={{ base: 'lg', md: 'xl' }} fontWeight='700' letterSpacing="0.08em" textTransform='uppercase'>
                    Velocorner
                  </Text>
                  <Text fontSize="xs" color="slate.500" textTransform="uppercase" letterSpacing="0.12em">
                    Endurance analytics
                  </Text>
                </VStack>
              </HStack>
            </Link>

            <HStack gap={3} display={{ base: 'none', md: 'flex' }}>
              {links.map((link) => (
                <Link
                  key={link.href}
                  href={link.href}
                  px={4}
                  py={2}
                  borderRadius="full"
                  color="slate.600"
                  fontWeight="600"
                  _hover={{ bg: 'white', color: 'slate.900' }}
                >
                  {link.label}
                </Link>
              ))}
              {isAdmin && (
                <Link
                  href="/admin"
                  px={4}
                  py={2}
                  borderRadius="full"
                  color="slate.600"
                  fontWeight="600"
                  _hover={{ bg: 'white', color: 'slate.900' }}
                >
                  Admin
                </Link>
              )}
              {!isAuthenticated && (
                <Button
                  colorPalette="orange"
                  size="md"
                  fontWeight="700"
                  borderRadius="full"
                  px={5}
                  boxShadow="0 14px 28px rgba(252, 121, 52, 0.25)"
                  onClick={connect}
                  loading={authLoading}
                >
                  <Image src={strava} boxSize="20px" mr={2} />
                  Connect Strava
                </Button>
              )}
            </HStack>

            <IconButton
              display={{ base: 'flex', md: 'none' }}
              onClick={() => setIsMenuOpen(!isMenuOpen)}
              aria-label="Toggle menu"
              variant="ghost"
              borderRadius="full"
              color="slate.700"
            >
              {isMenuOpen ? <LuX /> : <LuMenu />}
            </IconButton>
          </Flex>
        </Flex>

        {isMenuOpen && (
          <VStack
            display={{ base: 'flex', md: 'none' }}
            mt={3}
            px={5}
            py={5}
            gap={3}
            border="1px solid"
            borderColor="rgba(20, 32, 51, 0.08)"
            bg="rgba(255,255,255,0.92)"
            backdropFilter="blur(18px)"
            borderRadius="2xl"
            boxShadow="0 18px 40px rgba(20, 32, 51, 0.08)"
            alignItems="stretch"
          >
            {links.map((link) => (
              <Link
                key={link.href}
                href={link.href}
                px={3}
                py={2}
                borderRadius="xl"
                color="slate.700"
                fontWeight="600"
                _hover={{ bg: 'slate.50' }}
              >
                {link.label}
              </Link>
            ))}
            {isAdmin && (
              <Link href="/admin" px={3} py={2} borderRadius="xl" color="slate.700" fontWeight="600" _hover={{ bg: 'slate.50' }}>
                Admin
              </Link>
            )}
            {!isAuthenticated && (
              <Button
                colorPalette="orange"
                size="md"
                fontWeight="700"
                borderRadius="full"
                onClick={connect}
                loading={authLoading}
              >
                <Image src={strava} boxSize="20px" mr={2} />
                Connect with Strava
              </Button>
            )}
          </VStack>
        )}
      </Container>
    </Box>
  )
}

export default Header;
