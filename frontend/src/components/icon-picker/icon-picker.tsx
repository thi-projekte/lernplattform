import { ActionIcon, Input, SimpleGrid, Tooltip } from '@mantine/core';
import { CONTENT_ICONS } from './icons.ts';

interface IconPickerProps {
  label?: string;
  value: string | null;
  onChange: (name: string) => void;
  error?: React.ReactNode;
  required?: boolean;
}

const IconPicker = ({ label, value, onChange, error, required }: IconPickerProps) => {
  return (
    <Input.Wrapper label={label} required={required} error={error}>
      <SimpleGrid cols={6} spacing="xs" mt={4}>
        {Object.entries(CONTENT_ICONS).map(([name, Icon]) => {
          const isSelected = value === name;
          return (
            <Tooltip key={name} label={name.replace(/^Icon/, '')} withArrow>
              <ActionIcon
                variant={isSelected ? 'filled' : 'subtle'}
                color={isSelected ? 'blue' : 'gray'}
                size="lg"
                aria-label={name}
                onClick={() => onChange(name)}
              >
                <Icon size={20} />
              </ActionIcon>
            </Tooltip>
          );
        })}
      </SimpleGrid>
    </Input.Wrapper>
  );
};

export default IconPicker;
