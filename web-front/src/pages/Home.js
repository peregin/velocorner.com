import React from 'react';

const Home = () => {

    const handleClick = () => console.log("TEST")

    return (
        <div>
            <h2>Home</h2>
            <button onClick={handleClick}>Test JWT</button>
        </div>
    )
}

export default Home;