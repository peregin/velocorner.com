import { Link, Image } from '@chakra-ui/react';
import { Tooltip } from '@/components/ui/tooltip'

type SocialProps = {
  href: string;
  image: string;
  alt: string;
};

export default function Social({ href, image, alt }: SocialProps) {
  return (
    <Link
      href={href}
      target="_blank"
      rel="noopener noreferrer"
      p={3}
      bg="gray.800"
      borderRadius="lg"
      color="gray.400"
      _hover={{ 
        color: 'white', 
        bg: 'gray.700',
        '& img': {
          transform: 'rotateY(180deg)'
        }
      }}
      transition="all 0.2s"
    >
      <Tooltip content={alt}>
        <Image
          src={image}
          boxSize={{ base: '10px', md: '15px', lg: '20px' }}
          boxShadow='md'
          borderRadius='full'
          alt={alt}
          transition="transform 0.3s ease"
        />
      </Tooltip>
    </Link>
  );
}


