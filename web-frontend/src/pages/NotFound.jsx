import React from "react";
import { Box, Heading, Text, Button, VStack } from "@chakra-ui/react";
import { Link } from "react-router-dom";

const NotFound = () => {
  return (
    <Box maxW="800px" mx="auto" p={10} textAlign="center">
      <VStack spacing={4}>
        <Heading size="2xl">404</Heading>
        <Text fontSize="lg">That’s an error! The requested URL was not found on this server.</Text>
        <Link to="/">
          <Button colorScheme="blue" variant="outline">Back Home</Button>
        </Link>
      </VStack>
    </Box>
  );
};

export default NotFound;


