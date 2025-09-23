import { React } from "react";
import { Box, LinkBox, LinkOverlay, Image, Badge, Flex, Tooltip, Icon } from "@chakra-ui/react";
import { FaStar, FaCheckCircle } from "react-icons/fa";

const ProductCard = ({ productName, productUrl, brandName, marketName, formattedPrice, imageUrl, imageAlt, reviewStars, isNew, onSales }) => {
  return (
    <LinkBox maxW='sm' borderWidth='1px' borderRadius='lg' overflow='hidden'>
      <LinkOverlay href={productUrl}>
        <Image src={imageUrl} alt={imageAlt} maxHeight='240px' />
      </LinkOverlay>

      <Box p='6'>
        <Box display='flex' alignItems='baseline'>
          {isNew && <Badge borderRadius='full' px='2' colorPalette='teal'>New</Badge>}
          {onSales && <Badge borderRadius='full' px='2' colorPalette='orange'>SALE</Badge>}
          <Tooltip label='Available'>
            <Icon color='green' px='2px'>
              <FaCheckCircle />
            </Icon>
          </Tooltip>
          <Box color='gray.500' fontWeight='semibold' letterSpacing='wide' fontSize='xs' textTransform='uppercase' ml='2'>
            {brandName}
          </Box>
        </Box>
        <Box mt='1' color='gray.500' fontWeight='semibold' as='h4' lineHeight='tight' noOfLines={1}>
          {productName}
        </Box>

        <Box fontWeight='bold'>{formattedPrice}</Box>

        <Flex mt='2' dir='row'>
          <Box alignItems='center'>
            {Array(5).fill('').map((_, i) => (
              <Icon
                key={i}
                color={i < reviewStars ? 'teal.500' : 'gray.300'}
              >
                <FaStar />
              </Icon>
            ))}
          </Box>
        </Flex>
        <Box alignContent='right' color='green.600' fontWeight='semibold' fontSize='xs'>{marketName}</Box>

      </Box>
    </LinkBox>
  )
}



export default ProductCard;
