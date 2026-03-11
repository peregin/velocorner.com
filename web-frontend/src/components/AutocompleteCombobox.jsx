import { Combobox, Portal, createListCollection } from '@chakra-ui/react';

const defaultItemToString = (item) => (item && item.value) || item || '';
const defaultItemToValue = (item) => (item && item.value) || item || '';

/**
 * Reusable Autocomplete Combobox built on Chakra UI Combobox.
 *
 * Props:
 * - value: string - current input value
 * - items: array - list of suggestions (string or { value: string })
 * - placeholder: string
 * - width: string | number
 * - emptyMessage: string - message shown when list is empty
 * - onInputValueChange: (value: string) => void
 * - onSelect: (value: string) => void
 * - onKeyPress: (event) => void
 * - itemToString: (item) => string
 * - itemToValue: (item) => string
 * - allowFreeText: boolean - keep arbitrary typed text instead of auto-selecting a suggestion
 */
const AutocompleteCombobox = ({
  value,
  items = [],
  placeholder = 'Enter value',
  emptyMessage = 'No items found',
  onInputValueChange,
  onSelect,
  onKeyPress,
  itemToString = defaultItemToString,
  itemToValue = defaultItemToValue,
  allowFreeText = true,
}) => {
  const collection = createListCollection({
    items,
    itemToString,
    itemToValue,
  });

  return (
    <Combobox.Root
      collection={collection}
      onInputValueChange={(details) => {
        onInputValueChange && onInputValueChange(details.inputValue);
      }}
      onValueChange={(details) => {
        const v = details?.value?.[0];
        if (v != null && onSelect) onSelect(v);
      }}
      inputBehavior={allowFreeText ? "none" : "autohighlight"}
    >
      <Combobox.Control>
        <Combobox.Input
          placeholder={placeholder}
          value={value}
          color="slate.900"
          bg="whiteAlpha.950"
          borderColor="blackAlpha.200"
          _placeholder={{ color: 'slate.500' }}
          onKeyDown={(event) => {
            if (allowFreeText && event.key === 'Enter') {
              const enteredValue = event.currentTarget.value;
              if (enteredValue?.trim() && onSelect) {
                event.preventDefault();
                onSelect(enteredValue);
              }
              return;
            }

            onKeyPress && onKeyPress(event);
          }}
        />
        <Combobox.IndicatorGroup>
          <Combobox.ClearTrigger />
          <Combobox.Trigger />
        </Combobox.IndicatorGroup>
      </Combobox.Control>
      <Portal>
        <Combobox.Positioner>
          <Combobox.Content
            bg="white"
            color="slate.900"
            border="1px solid"
            borderColor="blackAlpha.200"
            boxShadow="lg"
          >
            <Combobox.Empty color="slate.600">{emptyMessage}</Combobox.Empty>
            {items.map((item, idx) => (
              <Combobox.Item
                key={idx}
                item={item}
                color="slate.900"
                _hover={{ bg: 'slate.100' }}
                _highlighted={{ bg: 'slate.100', color: 'slate.900' }}
              >
                {itemToString(item)}
                <Combobox.ItemIndicator />
              </Combobox.Item>
            ))}
          </Combobox.Content>
        </Combobox.Positioner>
      </Portal>
    </Combobox.Root>
  );
};

export default AutocompleteCombobox;
