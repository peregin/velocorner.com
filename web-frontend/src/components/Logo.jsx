import React, { useReducer, useEffect } from 'react'
import { Image, Link, Tooltip } from '@chakra-ui/react'

const Logo = ({ image, name, link, rotation = 90, timing = 200 }) => {

    const initialState = {
        isMouseJustOver: false, // is true for a short time after mouse is entering
        isMouseStillOver: false // is always true when the mouse is over
    }
    const reducer = (state, newState) => ({ ...state, ...newState })
    const [state, setState] = useReducer(reducer, initialState)

    useEffect(() => {
        if (!state.isMouseJustOver) {
            return;
        }
        const timeoutId = window.setTimeout(() => {
            setState({ isMouseJustOver: false })
        }, timing)
        return () => {
            window.clearTimeout(timeoutId)
        }
    }, [state.isMouseJustOver, timing])

    const style = {
        display: 'inline-block',
        backfaceVisibility: 'hidden',
        transform: state.isMouseJustOver ? `rotateY(${rotation}deg)` : `rotateY(0deg)`,
        transition: `transform ${timing}ms`,
        filter: state.isMouseStillOver ? `grayscale(0%)` : `grayscale(95%)`,
    }

    const handleMouseEnter = () => {
        setState({
            isMouseJustOver: true,
            isMouseStillOver: true
        });
    }


    const handleMouseLeave = () => {
        setState({
            isMouseJustOver: false,
            isMouseStillOver: false
        });
    }

    return (
        <Link href={link} onMouseEnter={handleMouseEnter} onMouseLeave={handleMouseLeave}>
            <Tooltip label={name}>
                <Image src={image} boxSize={{ base: '30px', md: '40px', lg: '40px' }} boxShadow='md' borderRadius='full' alt={name} style={style} />
            </Tooltip>
        </Link>
    )
}

export default Logo