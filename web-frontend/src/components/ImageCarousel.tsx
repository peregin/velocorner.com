import { forwardRef } from "react";
import {
    Image,
    Box,
    IconButton,
    IconButtonProps,
    Carousel,
    AspectRatio,
    Text,
} from "@chakra-ui/react";
import { LuArrowLeft, LuArrowRight } from "react-icons/lu";

interface CarouselItem {
    image: string;
    label: string;
}

interface ImageCarouselProps {
    items?: CarouselItem[];
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
    { image: "images/background/davos.webp", label: "Davos" },
    { image: "images/background/colnago.webp", label: "Colnago" },
    { image: "images/background/gemmi.webp", label: "Gemmipass" },
    { image: "images/background/stelvio2.webp", label: "Stelvio" },
    { image: "images/background/lenzerheide.webp", label: "Lenzerheide" },
    { image: "images/background/surenenpass3.webp", label: "Surenpass" },
    { image: "images/background/sustenpass.webp", label: "Sustenpass" },
    { image: "images/background/schwialp.webp", label: "Schwialppass" },
    { image: "images/background/colnagov1r.webp", label: "Colnago V1R" },
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
                    {items.map((item, index) => (
                        <Carousel.Item key={index} index={index}>
                            <AspectRatio ratio={25 / 3} maxH="72vh" w="full" position="relative">
                                <>
                                    <Image
                                        src={item.image}
                                        alt={item.label}
                                        objectFit="contain"
                                    />
                                    <Box
                                        position="absolute"
                                        right="0"
                                        top="0"
                                        transform="translateX(20%)"
                                        // bg="blackAlpha.700"
                                        color="white"
                                        px={4}
                                        py={3}
                                        textAlign="center"
                                    >
                                        <Text fontSize="xl" fontWeight="bold">
                                            {item.label}
                                        </Text>
                                    </Box>
                                </>
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

