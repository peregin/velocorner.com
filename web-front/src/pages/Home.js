import React from 'react';

const Home = ({ match }) => {
    return <div>Current Route: {match.params.tech}</div>
};

export default Home