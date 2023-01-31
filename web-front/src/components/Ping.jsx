import React, { useState, useEffect } from 'react';
import { ListItem, List, ListIcon } from "@chakra-ui/react";
import { CheckCircleIcon } from '@chakra-ui/icons'

const Ping = () => {
    const [date, setDate] = useState(new Date());

    useEffect(() => {
        let id = setInterval(() => setDate(new Date()), 1000);
        return () => clearInterval(id);
    }, []);

    return (
        <List spacing={3}>
            <ListItem>
                <ListIcon as={CheckCircleIcon} color='green.500' />
                Time: {date.toLocaleTimeString()}
            </ListItem>
            <ListItem>
                <ListIcon as={CheckCircleIcon} color='green.500' />
                Ping: OK
            </ListItem>
        </List>
    )
}

export default Ping;