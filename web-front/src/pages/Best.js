import { React, useState, useEffect } from "react";
import { Flex, Heading, Input, IconButton, Box, Text, Progress, SimpleGrid, Image, Badge } from "@chakra-ui/react";
import { SearchIcon, StarIcon } from "@chakra-ui/icons";
import ApiClient from "../service/ApiClient";

const Best = () => {

  const [input, setInput] = useState('')
  const [query, setQuery] = useState('')
  const [results, setResults] = useState([])
  useEffect(() => {
    fetchData()
  }, [query])

  const fetchData = async () => {
    if (query.trim === '') {
      return
    }
    let results = await ApiClient.search(query)
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

  const ProductCard = () => {
    const property = {
      imageUrl: 'https://bit.ly/2Z4KKcF',
      imageAlt: 'Rear view of modern home with pool',
      beds: 3,
      baths: 2,
      title: 'Modern home in city center in the heart of historic Los Angeles',
      formattedPrice: '$1,900.00',
      reviewCount: 34,
      rating: 4,
    }
    return (
      <Box maxW='sm' borderWidth='1px' borderRadius='lg' overflow='hidden'>
        <Image src={property.imageUrl} alt={property.imageAlt} />
  
        <Box p='6'>
          <Box display='flex' alignItems='baseline'>
            <Badge borderRadius='full' px='2' colorScheme='teal'>
              New
            </Badge>
            <Box
              color='gray.500'
              fontWeight='semibold'
              letterSpacing='wide'
              fontSize='xs'
              textTransform='uppercase'
              ml='2'
            >
              {property.beds} beds &bull; {property.baths} baths
            </Box>
          </Box>
  
          <Box
            mt='1'
            fontWeight='semibold'
            as='h4'
            lineHeight='tight'
            noOfLines={1}
          >
            {property.title}
          </Box>
  
          <Box>
            {property.formattedPrice}
            <Box as='span' color='gray.600' fontSize='sm'>
              / wk
            </Box>
          </Box>
  
          <Box display='flex' mt='2' alignItems='center'>
            {Array(5)
              .fill('')
              .map((_, i) => (
                <StarIcon
                  key={i}
                  color={i < property.rating ? 'teal.500' : 'gray.300'}
                />
              ))}
            <Box as='span' ml='2' color='gray.600' fontSize='sm'>
              {property.reviewCount} reviews
            </Box>
          </Box>
        </Box>
      </Box>
    )
  }

  return (
      <Flex align="center" gap="10" direction="column" margin={10}>
        <Heading>
          Find the best price for bikes, components, accessories, clothing,
          brands and items 🎉
        </Heading>

        <Box w="75%" border='1px' borderColor='lightgray' borderLeft='2px' borderLeftColor='green' p='20'>
        <Text m='5' fontSize='1.2em'>Search for the best price on your favorite brands, components, bicycles and accessories:</Text>
          <Flex>
            <Input value={input} onChange={(ev) => setInput(ev.target.value)} onKeyDown={handleEnter} placeholder='Type in your query...'/>
            <IconButton colorScheme="green" aria-label="Search components" icon={<SearchIcon onClick={handleSearch} />}

            />
          </Flex>
        </Box>

        <Progress hasStripe value={50} w='75%'/>
        <Text color='gray'>5 RESULTS IN 8.70 SECONDS</Text>

        <SimpleGrid columns={4} spacing={10}>
          <ProductCard/>
          <ProductCard/>
        </SimpleGrid>

      </Flex>
    )
}

export default Best;
