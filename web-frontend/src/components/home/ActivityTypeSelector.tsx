import { Box, Portal, Select, Tabs, createListCollection } from "@chakra-ui/react";
import { useMemo } from "react";

interface HomeActivityTypeSelectorProps {
  activityTypes: string[];
  selectedActivityType: string;
  onChange: (activityType: string) => void;
}

const ActivityTypeSelector = ({
  activityTypes,
  selectedActivityType,
  onChange,
}: HomeActivityTypeSelectorProps) => {
  const activityTypeCollection = useMemo(
    () =>
      createListCollection({
        items: activityTypes.map((activityType) => ({
          value: activityType,
          label: activityType,
        })),
      }),
    [activityTypes]
  );

  return (
    <>
      <Box display={{ base: "block", md: "none" }} width="100%">
        <Select.Root
          collection={activityTypeCollection}
          value={[selectedActivityType]}
          onValueChange={(event) => {
            if (event.value?.[0]) {
              onChange(event.value[0]);
            }
          }}
          size="md"
          width="100%"
        >
          <Select.HiddenSelect />
          <Select.Control>
            <Select.Trigger
              borderRadius="full"
              bg="rgba(255,255,255,0.72)"
              border="1px solid"
              borderColor="rgba(20, 32, 51, 0.08)"
            >
              <Select.ValueText placeholder="Select activity type" ml='1rem'/>
              <Select.IndicatorGroup>
                <Select.Indicator />
              </Select.IndicatorGroup>
            </Select.Trigger>
          </Select.Control>
          <Portal>
            <Select.Positioner>
              <Select.Content bg='white' color='black'>
                {activityTypes.map((activityType) => (
                  <Select.Item key={activityType} item={{ value: activityType, label: activityType }}>
                    {activityType}
                    <Select.ItemIndicator color='blue'/>
                  </Select.Item>
                ))}
              </Select.Content>
            </Select.Positioner>
          </Portal>
        </Select.Root>
      </Box>

      <Box display={{ base: "none", md: "block" }}>
        <Tabs.Root variant="outline" value={selectedActivityType} onValueChange={(event) => event.value && onChange(event.value)}>
          <Tabs.List bg="rgba(255,255,255,0.72)" rounded="full" p={1} border="1px solid" borderColor="rgba(20, 32, 51, 0.08)">
            {activityTypes.map((activityType) => (
              <Tabs.Trigger
                key={activityType}
                value={activityType}
                color="slate.700"
                px={5}
                py={2}
                borderRadius="full"
                _selected={{ bg: "slate.900", color: "white", boxShadow: "sm" }}
              >
                {activityType}
              </Tabs.Trigger>
            ))}
          </Tabs.List>
        </Tabs.Root>
      </Box>
    </>
  );
};

export default ActivityTypeSelector;
