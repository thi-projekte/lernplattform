import { Badge, Button, Group, Text, Title } from '@mantine/core';
import type { Topic } from '../../../schemas/topic';
import { useTranslation } from 'react-i18next';
import { IconEdit } from '@tabler/icons-react';
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
    </>
  );
};

export default TopicSidebarContent;
