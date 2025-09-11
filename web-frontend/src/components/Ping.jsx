import React, { useState, useEffect } from 'react';
import { ListItem, List, Icon } from "@chakra-ui/react";
import { FaCheckCircle } from 'react-icons/fa'

const Ping = () => {
    const [date, setDate] = useState(new Date());

    useEffect(() => {
        let id = setInterval(() => setDate(new Date()), 1000);
        return () => clearInterval(id);
    }, []);

    return (
        <List spacing={3}>
            <ListItem>
                <Icon as={FaCheckCircle} color='green.500' />
                Time: {date.toLocaleTimeString()}
            </ListItem>
            <ListItem>
                <Icon as={FaCheckCircle} color='green.500' />
                Ping: OK
            </ListItem>
        </List>
    )
}

export default Ping;
