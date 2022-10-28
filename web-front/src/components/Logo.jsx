import React, { useState, useEffect } from 'react';
import strava from "super-tiny-icons/images/svg/strava.svg";
import { Image, Link } from '@chakra-ui/react'

const Logo = ({ rotation = 90, timing = 200 }) => {

    const [isBooped, setIsBooped] = useState(false)

    useEffect(() => {
        if (!isBooped) {
          return;
        }
        const timeoutId = window.setTimeout(() => {
          setIsBooped(false);
        }, timing)
        return () => {
          window.clearTimeout(timeoutId);
        }
      }, [isBooped, timing])

    const style = {
        display: 'inline-block',
        backfaceVisibility: 'hidden',
        transform: isBooped ? `rotateY(${rotation}deg)` : `rotateY(0deg)`,
        transition: `transform ${timing}ms`,
        filter: isBooped ? `grayscale(0%)` : `grayscale(95%)`,
      }

    const handleMouseEnter = () => {
        setIsBooped(true)
     }

    return (
        <Link src='https://www.strava.com/clubs/velocorner' onMouseEnter={handleMouseEnter}>
            <Image src={strava} boxSize='40px' boxShadow='md' borderRadius='full' alt='Strava' style={style}/>
        </Link>
    )
}

export default Logo