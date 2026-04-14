import { UnstyledButton, Menu, Group, Text } from '@mantine/core';
import { useTranslation } from 'react-i18next';
import { IconChevronDown, IconWorld } from '@tabler/icons-react';

const supportedLanguages = [
  { label: 'Deutsch', value: 'de', flag: '🇩🇪' },
  { label: 'English', value: 'en', flag: '🇺🇸' },
];

const LanguagePicker = () => {
  const { i18n } = useTranslation();
  const activeLang =
    supportedLanguages.find((l) => l.value === i18n.language) || supportedLanguages[0];

  return (
    <Menu shadow="md" width={150}>
      <Menu.Target>
        <UnstyledButton>
          <Group gap="xs">
            <IconWorld size={20} stroke={1.5} />
            <Text size="sm" fw={500} visibleFrom="xs">
              {activeLang.value.toUpperCase()}
            </Text>
            <IconChevronDown size={14} stroke={1.5} />
          </Group>
        </UnstyledButton>
      </Menu.Target>

      <Menu.Dropdown>
        {supportedLanguages.map((lang) => (
          <Menu.Item
            key={lang.value}
            leftSection={lang.flag}
            onClick={() => i18n.changeLanguage(lang.value)}
          >
            {lang.label}
          </Menu.Item>
        ))}
      </Menu.Dropdown>
    </Menu>
  );
};

export default LanguagePicker;
