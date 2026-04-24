import { Badge, Button, Group, Text, Title } from '@mantine/core';
import type { Topic } from '../../../schemas/topic';
import { useTranslation } from 'react-i18next';
import { IconEdit } from '@tabler/icons-react';
import { useNavigate } from 'react-router';

interface TopicSidebarContentProps {
  selectedElement: Topic;
}

const TopicSidebarContent = ({ selectedElement }: TopicSidebarContentProps) => {
  const { t } = useTranslation();
  const navigate = useNavigate();

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
        Minuten
      </Text>
      <Text size="sm">{selectedElement.teaser}</Text>

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
    </>
  );
};

export default TopicSidebarContent;
