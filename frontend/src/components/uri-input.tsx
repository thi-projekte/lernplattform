import { useTranslation } from 'react-i18next';
import { ActionIcon, rem, TextInput } from '@mantine/core';
import { IconLink, IconX } from '@tabler/icons-react';

interface UriInputProps {
  value?: string;
  onChange: (value: string) => void;
  error?: string;
}

const UriInput = ({ value, onChange, error }: UriInputProps) => {
  const { t } = useTranslation();

  return (
    <TextInput
      type="url"
      label={t('common.uri')}
      placeholder="https://example.com/resource"
      leftSection={<IconLink style={{ width: rem(18), height: rem(18) }} />}
      value={value}
      onChange={(event) => onChange(event.currentTarget.value)}
      error={error}
      rightSectionPointerEvents="all"
      rightSection={
        value ? (
          <ActionIcon variant="transparent" color="dimmed" onClick={() => onChange('')}>
            <IconX style={{ width: rem(16), height: rem(16) }} />
          </ActionIcon>
        ) : null
      }
    />
  );
};

export default UriInput;
