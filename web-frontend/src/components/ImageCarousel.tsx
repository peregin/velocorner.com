import { forwardRef } from "react";
import {
  Image,
  Box,
  IconButton,
  IconButtonProps,
  Carousel,
  AspectRatio
} from "@chakra-ui/react";
import { LuArrowLeft, LuArrowRight } from "react-icons/lu";

interface ImageCarouselProps {
  items?: string[];
}

const ActionButton = forwardRef<HTMLButtonElement, IconButtonProps>(
  function ActionButton(props, ref) {
    return (
      <IconButton
        {...props}
        ref={ref}
        size="xs"
        variant="outline"
        rounded="full"
        position="absolute"
        zIndex="1"
        bg="bg"
      />
    )
  },
)

const ImageCarousel = ({ items = [
    "images/background/davos.webp",
  "images/background/colnago.webp",
  "images/background/gemmi.webp",
  "images/background/stelvio2.webp",
  "images/background/lenzerheide.webp",
  "images/background/surenenpass3.webp",
  "images/background/sustenpass.webp",
  "images/background/schwialp.webp",
  "images/background/colnagov1r.webp",
] }: ImageCarouselProps) => {
  return (
    <Carousel.Root
      slideCount={items.length}
      maxW="100%"
      mx="auto"
      gap="4"
      pt='.5rem'
      position="relative"
      colorPalette="white"
    >
      <Carousel.Control gap="4" width="full" position="relative">
        <Carousel.PrevTrigger asChild>
          <ActionButton insetStart="4">
            <LuArrowLeft />
          </ActionButton>
        </Carousel.PrevTrigger>

        <Carousel.ItemGroup width="full">
          {items.map((src, index) => (
            <Carousel.Item key={index} index={index}>
              <AspectRatio ratio={25 / 3} maxH="72vh" w="full">
                <Image
                  src={src}
                  alt={`Product ${index + 1}`}
                  objectFit="contain"
                />
              </AspectRatio>
            </Carousel.Item>
          ))}
        </Carousel.ItemGroup>

        <Carousel.NextTrigger asChild>
          <ActionButton insetEnd="4">
            <LuArrowRight />
          </ActionButton>
        </Carousel.NextTrigger>

        <Box position="absolute" bottom="6" width="full">
          <Carousel.Indicators
            transition="width 0.2s ease-in-out"
            transformOrigin="center"
            opacity="0.5"
            boxSize="2"
            _current={{ width: "10", bg: "colorPalette.subtle", opacity: 1 }}
          />
        </Box>
      </Carousel.Control>
    </Carousel.Root>
  );
};

export default ImageCarousel;

