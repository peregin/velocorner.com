"use client"

import { ChakraProvider, defaultSystem } from "@chakra-ui/react"
import {
  ColorModeProvider,
  type ColorModeProviderProps,
} from "./color-mode"

interface ProviderProps extends ColorModeProviderProps {
  value?: any
}

export function Provider({ value, ...props }: ProviderProps) {
  return (
    <ChakraProvider value={value || defaultSystem}>
      <ColorModeProvider {...props} />
    </ChakraProvider>
  )
}
