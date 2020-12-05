import React from 'react';

class Home extends React.Component {
    handleClick() {
        console.log("TEST")
    }
    render() {
        return <div>
            <h2>Home</h2>
            <button onClick={this.handleClick}>Test JWT</button>
        </div>
    }
};

export default Home;