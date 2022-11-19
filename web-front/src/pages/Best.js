import { React, useState } from "react";
import { Flex, Heading, Input, IconButton, Box, Text, Progress } from "@chakra-ui/react";
import { SearchIcon } from "@chakra-ui/icons";

const Best = () => {

  const [query, setQuery] = useState('')

  const handleEnter = (ev) => {
    if (ev.key === 'Enter') {
      let q = ev.target.value
      console.info('searching for ' + q + '...')
      setQuery(q)
    }
  }

  const handleSearch = (ev) => {
    console.info('searching...')
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
            <Input placeholder="Type in your query..." onKeyDown={handleEnter}/>
            <IconButton colorScheme="green" aria-label="Search components" icon={<SearchIcon onClick={handleSearch} />}

            />
          </Flex>
        </Box>

        <Progress hasStripe value={50} w='75%'/>
      </Flex>
    )
}

export default Best;
