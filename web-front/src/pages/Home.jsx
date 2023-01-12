import React, { useState, useEffect } from "react";
import ApiClient from "../service/ApiClient";

import { useOAuth2 } from "@tasoskakour/react-use-oauth2";
import { Button, Heading, Text, Tag, Divider } from "@chakra-ui/react";
import { Progress } from "@chakra-ui/react";

const Home = () => {
  const [memoryUsage, setMemoryUsage] = useState(50);

  const { data, loading, error, getAuth } = useOAuth2({
    authorizeUrl: ApiClient.stravaBaseAuthUrl,
    clientId: ApiClient.stravaClientId,
    redirectUri: 'http://localhost:9001/fe/oauth/strava', //`${document.location.origin}/oauth/strava`,
    scope: 'read,activity:read,profile:read_all',
    responseType: 'code',
    extraQueryParameters: 'approval_prompt=auto',
    exchangeCodeForTokenServerURL: "http://localhost:9001/api/token/strava",
    exchangeCodeForTokenMethod: "POST",
    onSuccess: (payload) => console.log("Success", payload),
    onError: (error_) => console.error("Error", error_)
  });
  const isLoggedIn = Boolean(data?.access_token);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    let summary = await ApiClient.status();
    console.log(summary);
    setMemoryUsage(summary.memoryUsedPercentile);
  };

  const handleClick = (_ev) => {
    console.log("LOGGING IN...");
    getAuth()
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

      {loading && <Text>Loading...</Text>}
      {error && <Text>Error... {error}</Text>}
      {isLoggedIn && <Text>{JSON.stringify(data)}</Text>}
    </div>
  );
};

export default Home;
