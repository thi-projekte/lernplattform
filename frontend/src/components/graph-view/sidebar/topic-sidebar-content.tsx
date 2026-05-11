import { Badge, Button, Group, Text, Title, Divider, Stack, ThemeIcon } from '@mantine/core';
import type { Topic } from '../../../schemas/topic';
import { useTranslation } from 'react-i18next';
import { IconEdit, IconRobot } from '@tabler/icons-react';
import { useNavigate } from 'react-router';
import { useUserService } from '../../../provider/user-provider';

interface TopicSidebarContentProps {
  selectedElement: Topic;
}

const TopicSidebarContent = ({ selectedElement }: TopicSidebarContentProps) => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const userService = useUserService();
  const currentUsername = userService.account.username?.toLowerCase();
  const isOwner =
    !!selectedElement.creatorId &&
    !!currentUsername &&
    selectedElement.creatorId.toLowerCase() === currentUsername;

  return (
    <>
      <Title order={3}>{selectedElement.title}</Title>
      <Text size="sm" c="dimmed">
        {t('topic.fields.author')}: {selectedElement.creatorFullName}
      </Text>
      <Group>
        {selectedElement.categories?.map((cat, i) => (
          <Badge key={i} color={`#${cat.color}`}>
            {cat.title}
          </Badge>
        ))}
      </Group>
      <Text size="sm" c="dimmed">
        {t('topic.fields.estimatedLearningDuration')}: {selectedElement.estimatedLearningDuration}{' '}
        {t('topic.fields.estimatedLearningDurationSuffix')}
      </Text>
      <Text size="sm">{selectedElement.teaser}</Text>

      {isOwner && (
        <Button
          leftSection={<IconEdit size={16} />}
          variant="light"
          color="blue"
          fullWidth
          mt="xl"
          onClick={() => navigate(`/builder-mode/topics/${selectedElement.id}/edit`)}
        >
          {t('common.edit')}
        </Button>
      )}
      <Button color="blue" fullWidth mt="xl">
        {t('topic.actions.start')}
      </Button>
      <Divider mt="xl" />

      <Stack gap="sm" mt="md">
        <Group>
          <ThemeIcon color="blue" radius="xl">
            <IconRobot size={16} />
          </ThemeIcon>
          <Text fw={500}>Myna</Text>
          <Text size="xs" c="dimmed">
            {t('topic.myna.subtitle')}
          </Text>
        </Group>
        <Text size="sm" c="dimmed">
          {t('topic.myna.description')}
        </Text>
        <Button variant="outline" color="blue" fullWidth>
          {t('topic.myna.askButton')}
        </Button>
      </Stack>
    </>
  );
};

export default TopicSidebarContent;
