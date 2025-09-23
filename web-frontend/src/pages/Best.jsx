import { React, useState, useEffect, useCallback } from "react";
import {
  Flex,
  Heading,
  Input,
  IconButton,
  Box,
  Text,
  Progress,
  SimpleGrid,
} from "@chakra-ui/react";
import { LuSearch } from "react-icons/lu";
import ApiClient from "../service/ApiClient";
import ProductCard from "../components/ProductCard";
import SupportedMarkets from "../components/SupportedMarkets";

const Best = () => {
  const [input, setInput] = useState("");
  const [query, setQuery] = useState("");
  const [results, setResults] = useState([]);
  const [elapsed, setElapsed] = useState("");
  const [searching, setSearching] = useState(false);

  const fetchData = useCallback(async () => {
    if (query.trim() === "") {
      console.info("searching for empty term....");
    } else {
      let startTime = new Date().getTime();

      setSearching(true);
      console.info("searching for [" + query + "]...");
      let results = await ApiClient.searchProducts(query);
      window.analytics.track('Best', {
        term: query
      });
      setSearching(false);

      let elapsedTime = new Date().getTime() - startTime;
      let took = (elapsedTime / 1000).toFixed(2);
      if (results.length > 0)
        setElapsed(results.length + " results in " + took + " seconds");
      else setElapsed("");

      console.info(results.length + " results");
      setResults(results);
    }
  }, [query]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  const handleEnter = (ev) => {
    if (ev.key === "Enter") {
      console.info("enter searching for " + input + "...");
      setQuery(input);
    }
  };

  const handleSearch = (ev) => {
    console.info("click searching for " + input + "...");
    setQuery(input);
  };

  return (
    <Flex align="center" gap="5" direction="column" margin={[5, 10]}>
      <Heading as="h4" size={["sm", "md"]}>
        Find the best price for bikes, components, accessories, clothing, brands
        and items 🎉
      </Heading>

      <Box
        w="100%"
        border="1px"
        borderColor="lightgray"
        borderLeft="2px"
        borderLeftColor="green"
        pl={[2, 20]}
        pr={[2, 20]}
        pb={[2, 5]}
      >
        <Text m="5" fontSize={["sm", "1.2em"]}>
          Search for the best price on your favorite brands, components,
          bicycles and accessories:
        </Text>
        <Flex>
          <Input
            value={input}
            onChange={(ev) => setInput(ev.target.value)}
            onKeyDown={handleEnter}
            placeholder="Type in your query..."
          />
          <IconButton
            colorPalette="green"
            aria-label="Search components"
            icon={<LuSearch onClick={handleSearch} />}
          />
        </Flex>
      </Box>

      <Progress
        w="100%"
        hasStripe
        isIndeterminate
        visibility={searching ? "visible" : "hidden"}
      />
      {results.length > 0 && <Text color="gray">{elapsed}</Text>}

      <SimpleGrid columns={{ sm: 2, md: 4 }} spacing={[2, 10]}>
        {results.map((res, ix) => {
          return (
            <ProductCard
              key={ix}
              productName={res.name}
              productUrl={res.productUrl}
              brandName={res.brand?.name}
              marketName={res.market.name}
              formattedPrice={res.price.value + " " + res.price.currency}
              imageUrl={res.imageUrl}
              imageAlt={res.name}
              reviewStars={res.reviewStars}
              isNew={res.isNew}
              onSales={res.onSales}
            />
          );
        })}
      </SimpleGrid>

      <SupportedMarkets defIx={Math.floor(Math.random() * 5)} />
    </Flex>
  );
};

export default Best;
