import { useState, useEffect } from 'react';
import { Box, HStack, Icon } from "@chakra-ui/react";
import { FaCheckCircle } from 'react-icons/fa'

const Ping = () => {
    const [date, setDate] = useState(new Date());

    useEffect(() => {
        let id = setInterval(() => setDate(new Date()), 1000);
        return () => clearInterval(id);
    }, []);

    return (
        <Box>
            <HStack gap={2} mb={2}>
                <Icon as={FaCheckCircle} color='green.500' />
                Time: {date.toLocaleTimeString()}
            </HStack>
            <HStack gap={2}>
                <Icon as={FaCheckCircle} color='green.500' />
                Ping: OK
            </HStack>
        </Box>
    )
}

export default Ping;
