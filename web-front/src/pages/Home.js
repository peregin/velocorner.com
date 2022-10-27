import React from 'react';
import ApiClient from "../service/ApiClient";

import { Button, Heading } from '@chakra-ui/react'

const Home = () => {

    const handleClick = () => {
        console.log("LOGGING IN")
        ApiClient.test()
    }

    return (
        <div>
            <Heading>Home</Heading>
            <Button onClick={handleClick}>Test JWT</Button>
        </div>
    )
}

export default Home;