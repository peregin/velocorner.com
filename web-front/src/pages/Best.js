import { React, useState, useEffect } from "react";
import { Flex, Heading, Input, IconButton, Box, Text, Progress, SimpleGrid } from "@chakra-ui/react";
import { SearchIcon } from "@chakra-ui/icons";
import ApiClient from "../service/ApiClient";
import ProductCard from "../components/ProductCard";

const Best = () => {

  const [input, setInput] = useState('')
  const [query, setQuery] = useState('')
  const [results, setResults] = useState([])
  const [elapsed, setElapsed] = useState('')
  const [searching, setSearching] = useState(false)
  useEffect(() => {
    fetchData()
  }, [query])

  const fetchData = async () => {
    if (query.trim === '') {
      return
    }
    let startTime = new Date().getTime();

    setSearching(true)
    let results = await ApiClient.search(query) // todo: .catch(error...)
    setSearching(false)

    let elapsedTime = new Date().getTime() - startTime
    let took = (elapsedTime / 1000).toFixed(2)
    if (results.length > 0)
      setElapsed(results.length+' results in '+took+' seconds')
    else
      setElapsed('')

    console.info(results.length + ' results')
    setResults(results)
  }

  const handleEnter = (ev) => {
    if (ev.key === 'Enter') {
      console.info('enter searching for ' + input + '...')
      setQuery(input)
    }
  }

  const handleSearch = (ev) => {
    console.info('click searching for ' + input + '...')
    setQuery(input)
  }

  return (
      <Flex align="center" gap="10" direction="column" margin={10}>
        <Heading as='h4' size='md' noOfLines={1}>
          Find the best price for bikes, components, accessories, clothing,
          brands and items 🎉
        </Heading>

        <Box w="100%" border='1px' borderColor='lightgray' borderLeft='2px' borderLeftColor='green' p='20'>
        <Text m='5' fontSize='1.2em'>Search for the best price on your favorite brands, components, bicycles and accessories:</Text>
          <Flex>
            <Input value={input} onChange={(ev) => setInput(ev.target.value)} onKeyDown={handleEnter} placeholder='Type in your query...'/>
            <IconButton colorScheme="green" aria-label="Search components" icon={<SearchIcon onClick={handleSearch} />}

            />
          </Flex>
        </Box>

        <Progress w='100%' hasStripe isIndeterminate visibility={searching ? 'visible' : 'hidden'}/>
        <Text color='gray'>{elapsed}</Text>

        <SimpleGrid columns={4} spacing={10}>
          {results.map( (res, ix) => {
            //console.info('res=' + JSON.stringify(res.price) + ', ix=' + ix)
            return <ProductCard 
              key={ix}
              productName={res.name}
              productUrl={res.productUrl}
              brandName={res.brand.name}
              marketName={res.market.name}
              formattedPrice={res.price.value + ' ' + res.price.currency}
              imageUrl={res.imageUrl}
              imageAlt={res.name}
              reviewStars={res.reviewStars}
              isNew={res.isNew}
            />
          })}
        </SimpleGrid>

      </Flex>
    )
}

export default Best;
