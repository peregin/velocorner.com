import React, { useState, useEffect } from "react";
import ApiClient from "../service/ApiClient";

import { Button, Heading, Text, Tag, Divider } from "@chakra-ui/react";
import { Progress } from "@chakra-ui/react";

const Home = () => {
  const [memoryUsage, setMemoryUsage] = useState(50);
  useEffect(() => {
    fetchData();
  });

  const fetchData = async () => {
    let summary = await ApiClient.status();
    console.log(summary);
    setMemoryUsage(summary.memoryUsedPercentile);
  };

  const handleClick = (ev) => {
    console.log("LOGGING IN...");
    ApiClient.login();
  };

  return (
    <div>
      <Heading>Home</Heading>

      <h1>Welcome to Velocorner, memory usage {memoryUsage}%</h1>
      <Progress hasStripe value={memoryUsage} />

      <Button onClick={handleClick}>Test JWT Login</Button>

      <Divider m="10" />
      <Text>
        Commit Hash: <Tag colorScheme="teal">42</Tag>
      </Text>
    </div>
  );
};

export default Home;
