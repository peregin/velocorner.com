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
      inputBehavior="autohighlight"
      // width={width}
    >
      <Combobox.Control>
        <Combobox.Input
          placeholder={placeholder}
          value={value}
          onKeyPress={onKeyPress}
        />
        <Combobox.IndicatorGroup>
          <Combobox.ClearTrigger />
          <Combobox.Trigger />
        </Combobox.IndicatorGroup>
      </Combobox.Control>
      <Portal>
        <Combobox.Positioner>
          <Combobox.Content>
            <Combobox.Empty>{emptyMessage}</Combobox.Empty>
            {items.map((item, idx) => (
              <Combobox.Item key={idx} item={item}>
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
