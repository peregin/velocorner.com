import { Box, Heading, Text, VStack, Card } from "@chakra-ui/react";

const Privacy = () => {
  return (
    <Box maxW="1200px" mx="auto" p={6}>
      <VStack spacing={8} align="stretch">
        <Heading size="lg">Privacy Policy</Heading>

        <Card.Root>
          <Card.Body>
            <Heading size="md" mb={3}>Introduction</Heading>
            <Text>
              This Privacy Policy describes how your personal information is collected, used, and shared when you use our
              cycling data tracking hobby project, which utilizes GPX files and integrates with the Strava API.
            </Text>
          </Card.Body>
        </Card.Root>

        <Card.Root>
          <Card.Body>
            <Heading size="md" mb={3}>Information We Collect</Heading>
            <Text fontWeight="bold" mt={2}>GPX Files</Text>
            <Text>When you upload GPX files, we collect route information, GPS coordinates, timestamps, and elevation data.</Text>
            <Text fontWeight="bold" mt={4}>Strava Integration</Text>
            <Text>
              When you connect your Strava account, we access profile information (name, profile picture) and cycling activity details
              (routes, distances, durations, timestamps). We do not store your Strava login credentials.
            </Text>
          </Card.Body>
        </Card.Root>

        <Card.Root>
          <Card.Body>
            <Heading size="md" mb={3}>How We Use Your Information</Heading>
            <Text>
              We use the collected data to provide insights into your cycling activities, visualize routes and performance, and improve
              project features and functionality.
            </Text>
            <Heading size="sm" mt={4} mb={2}>Sharing Your Information</Heading>
            <Text>
              We do not share your personal data with third parties, except to comply with legal obligations or to enforce our policies.
              Your data from Strava and GPX files is never sold or distributed for commercial purposes.
            </Text>
          </Card.Body>
        </Card.Root>

        <Card.Root>
          <Card.Body>
            <Heading size="md" mb={3}>Data Security</Heading>
            <Text>
              We take reasonable measures to protect your data from unauthorized access or disclosure. However, as this is a hobby project,
              please avoid uploading sensitive or highly confidential information.
            </Text>
          </Card.Body>
        </Card.Root>

        <Card.Root>
          <Card.Body>
            <Heading size="md" mb={3}>Your Rights</Heading>
            <Text>
              You can request access to your personal data, request deletion or correction of your data, and disconnect your Strava account at any time.
            </Text>
          </Card.Body>
        </Card.Root>

        <Card.Root>
          <Card.Body>
            <Heading size="md" mb={3}>Changes to This Policy</Heading>
            <Text>
              We may update this Privacy Policy occasionally. Any changes will be posted on this page with an updated revision date.
            </Text>
          </Card.Body>
        </Card.Root>
      </VStack>
    </Box>
  );
};

export default Privacy;


