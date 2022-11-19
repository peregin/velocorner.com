import React, { Component } from "react";
import ApiClient from "../service/ApiClient";

import { Button, Heading, Text, Tag, Divider } from "@chakra-ui/react";
import { Progress } from "@chakra-ui/react";

class Home extends Component {
  constructor(props) {
    super(props);
    this.state = { memoryUsage: 50 };
  }

  async componentDidMount() {
    ApiClient.getStatus((summary) => {
      console.log(summary);
      this.setState({
        memoryUsage: summary.memoryUsedPercentile,
      });
    });
  }

  handleClick() {
    console.log("LOGGING IN");
    ApiClient.test();
  }

  render() {
    return (
      <div>
        <Heading>Home</Heading>

        <h1>Welcome to Velocorner, memory usage {this.state.memoryUsage}%</h1>
        <Progress hasStripe value={this.state.memoryUsage} />

        <Button onClick="handleClick">Test JWT</Button>
        <Divider m="10" />
        <Text>
          Commit Hash: <Tag colorScheme="teal">42</Tag>
        </Text>
      </div>
    );
  }
}

export default Home;
