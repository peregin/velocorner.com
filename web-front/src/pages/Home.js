import React from 'react';
import ApiClient from "../service/ApiClient";

import { Button, Heading, Text, Tag, Divider } from '@chakra-ui/react'

const Home = () => {

    const handleClick = () => {
        console.log('LOGGING IN')
        ApiClient.test()
    }

    return (
        <div>
            <Heading>Home</Heading>
            <Button onClick={handleClick}>Test JWT</Button>
            <Divider m='10'/>
            <Text>Commit Hash: <Tag colorScheme='teal'>42</Tag></Text>
        </div>
    )
}

export default Home;