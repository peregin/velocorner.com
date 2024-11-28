import { React, useEffect, useState } from "react";
import { LinkBox, LinkOverlay, Text, Image } from "@chakra-ui/react";
import ApiClient from "../service/ApiClient";

const SupportedMarkets = ({ defaultIx = 1, timing = 5000 }) => {
  const [data, setData] = useState([]);
  const [ix, setIx] = useState(defaultIx);

  useEffect(() => {
    ApiClient.markets().then((markets) => {
      let no = markets.length;
      console.info(`supporting ${no} markets`);
      setData(markets);
    });
  }, []);

  useEffect(() => {
    let id = setInterval(() => {
      setIx((oldIx) => {
        let newIx = 0;
        let no = data.length;
        if (oldIx < no - 1) {
          newIx = oldIx + 1;
        }
        //console.info(`setting market to ${newIx} out of ${no}`);
        return newIx;
      });
    }, timing);
    return () => clearInterval(id);
  }, [data, timing]);

  return (
    data.length > 0 && (
      <LinkBox
        maxW="sm"
        p="10"
        borderWidth="0"
        borderRadius="lg"
        overflow="hidden"
      >
        <LinkOverlay href={data[ix].url}></LinkOverlay>
        <Image src={data[ix].logoUrl} h="30px" />
        <Text align="center" fontWeight="bold" letterSpacing="wide">
          {data[ix].name}
        </Text>
      </LinkBox>
    )
  );
};

export default SupportedMarkets;
