import React, { useState, useEffect } from "react";
import ApiClient from "../service/ApiClient";
import ConnectWithStravaIcon from "../icons/ConnectWithStrava.tsx";

import { useOAuth2 } from "@tasoskakour/react-use-oauth2";
import { Button, Heading, Text, Tag, Divider, Image, IconButton } from "@chakra-ui/react";
import { Progress } from "@chakra-ui/react";
import strava from 'super-tiny-icons/images/svg/strava.svg'

const Home = () => {
  const [memoryUsage, setMemoryUsage] = useState(50);
  // just for testing
  const [wordCloud, setWordCloud] = useState([]);


  const { data, loading, error, getAuth } = useOAuth2({
    authorizeUrl: 'https://www.strava.com/api/v3/oauth/authorize',
    clientId: '4486',
    redirectUri: `${ApiClient.apiHost}/fe/oauth/strava`, //`${document.location.origin}/oauth/strava`,
    scope: 'read,activity:read,profile:read_all',
    responseType: 'code',
    extraQueryParameters: 'approval_prompt=auto',
    exchangeCodeForTokenServerURL: `${ApiClient.apiHost}/api/token/strava`,
    exchangeCodeForTokenMethod: "POST",
    onSuccess: (payload) => {
      console.log("Success", payload);
      localStorage.setItem('access_token', payload?.access_token);
    },
    onError: (error_) => console.error("Error", error_)
  });

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    let summary = await ApiClient.status();
    console.log(summary);
    setMemoryUsage(summary.memoryUsedPercentile);

    let wc = await ApiClient.wordcloud();
    console.log(wc.length);
    setWordCloud(wc);
  };

  const handleConnect = (_ev) => getAuth();

  return (
    <div>
      <Heading>Home</Heading>

      <h1>Welcome to Velocorner, memory usage {memoryUsage}%</h1>
      <Progress hasStripe value={memoryUsage} />

      <Button onClick={handleConnect}>Test JWT Login</Button>
      <Image src={strava} boxSize={{ base: '30px', md: '40px', lg: '40px' }} />
      <Button onClick={handleConnect} icon={strava} colorScheme='orange' variant='solid'>Connect</Button>
      <IconButton
        colorScheme='teal'
        aria-label='Call Segun'
        size='lg'
        icon={<ConnectWithStravaIcon />}
      />

      <Divider m="10" />
      <Text>
        Commit Hash: <Tag colorScheme="teal">42</Tag>
      </Text>

      <Divider m="10" />
      <Text>WordCloud (ltoken is [{data?.access_token.substring(0, 10)}...]):</Text>
      {loading && <Text>Loading...</Text>}
      {error && <Text>Error... {error}</Text>}

      <Text>
        {wordCloud.length}
      </Text>
    </div>
  );
};

export default Home;
