import React from 'react';
import ApiClient from "../service/ApiClient";

const Home = () => {

    const handleClick = () => {
        console.log("LOGGING IN")
        ApiClient.test()
    }

    return (
        <div>
            <h2>Home</h2>
            <button onClick={handleClick}>Test JWT</button>
        </div>
    )
}

export default Home;