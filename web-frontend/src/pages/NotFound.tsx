import { Box, Heading, Text, Button, VStack } from "@chakra-ui/react";
import { Link } from "react-router-dom";
import PageMeta from "@/components/PageMeta";

const NotFound = () => {
  return (
    <Box maxW="800px" mx="auto" p={10} textAlign="center">
      <PageMeta
        title="Page Not Found | Velocorner"
        description="The requested page could not be found on Velocorner."
      />
      <VStack gap={4}>
        <Heading size="2xl">404</Heading>
        <Text fontSize="lg">That’s an error! The requested URL was not found on this server.</Text>
        <Link to="/">
          <Button colorPalette="blue" variant="outline">Back Home</Button>
        </Link>
      </VStack>
    </Box>
  );
};

export default NotFound;


