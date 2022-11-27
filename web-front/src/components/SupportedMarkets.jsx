import { React, useEffect, useState } from "react";
import { LinkBox, LinkOverlay, Text, Image } from "@chakra-ui/react";
import ApiClient from "../service/ApiClient";

const SupportedMarkets = ({ defIx = 1, timing = 1000 }) => {

  const [data, setData] = useState([]);
  const [ix, setIx] = useState(defIx);

  useEffect(() => {
    ApiClient.markets().then((markets) => {
      let no = markets.length;
      console.info(`supporting ${no} markets`);
      setData(markets);

      // setInterval(() => {
      //   let newIx = 0;
      //   if (ix < no - 1) {
      //     newIx = ix + 1;
      //   }
      //   //console.info(`setting market to ${newIx} out of ${no}`)
      //   setIx(newIx);
      // }, timing);
    });
  }, []);

  return data.length > 0 && (
    <LinkBox maxW='sm' p='10' borderWidth='0' borderRadius='lg' overflow='hidden'>
      <LinkOverlay href={data[ix].url}></LinkOverlay>
      <Image src={data[ix].logoUrl} h='30px'/>
      <Text align='center' fontWeight='bold' letterSpacing='wide'>{data[ix].name}</Text>
    </LinkBox>
  )
}

export default SupportedMarkets;
