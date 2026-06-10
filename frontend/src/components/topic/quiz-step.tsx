import { type Dispatch, type SetStateAction, useState } from 'react';
import {
  ActionIcon,
  Button,
  Group,
  Paper,
  Stack,
  Text,
  Textarea,
  TextInput,
  Title,
  Tooltip,
} from '@mantine/core';
import { IconPlusFilled, IconTrash } from '@tabler/icons-react';
import { useTranslation } from 'react-i18next';
import { notifications } from '@mantine/notifications';
import type { IndexCardDto, Topic } from '../../schemas/topic.ts';
import { useCreateIndexCardMutation, useDeleteIndexCardMutation } from '../../api/topic.ts';

const MAX_CARDS = 20;

interface QuizStepProps {
  topic: Partial<Topic>;
  setTopic: Dispatch<SetStateAction<Partial<Topic>>>;
  onSave?: () => void;
  onSaveAndView?: () => void;
  isSubmitting?: boolean;
}

const QuizStep = ({ topic, setTopic, onSave, onSaveAndView, isSubmitting }: QuizStepProps) => {
  const { t } = useTranslation();
  const { mutateAsync: createIndexCard, isPending: isCreating } = useCreateIndexCardMutation();
  const { mutateAsync: deleteIndexCard, isPending: isDeleting } = useDeleteIndexCardMutation();

  const [question, setQuestion] = useState('');
  const [answer, setAnswer] = useState('');

  const indexCards: IndexCardDto[] = topic.indexCards ?? [];
  const reachedLimit = indexCards.length >= MAX_CARDS;
  const canAdd = question.trim().length > 0 && answer.trim().length > 0 && !reachedLimit;

  const handleAdd = async () => {
    if (!canAdd) return;
    try {
      const created = await createIndexCard({ question: question.trim(), answer: answer.trim() });
      setTopic((prev) => ({
        ...prev,
        indexCards: [...(prev.indexCards ?? []), created],
      }));
      setQuestion('');
      setAnswer('');
    } catch {
      notifications.show({ color: 'red', message: t('common.serverError') });
    }
  };

  const handleDelete = async (cardId: string | undefined) => {
    if (!cardId) return;
    try {
      await deleteIndexCard(cardId);
      setTopic((prev) => ({
        ...prev,
        indexCards: (prev.indexCards ?? []).filter((c) => c.id !== cardId),
      }));
    } catch {
      notifications.show({ color: 'red', message: t('common.serverError') });
    }
  };

  return (
    <Stack gap="md">
      <Group justify="space-between" align="center">
        <Title order={4}>{t('topic.steps.quizTitle')}</Title>
        <Text size="sm" c="dimmed">
          {t('topic.quiz.countLabel', { count: indexCards.length, max: MAX_CARDS })}
        </Text>
      </Group>

      <Paper withBorder p="md" radius="md">
        <Stack gap="sm">
          <TextInput
            label={t('topic.quiz.question')}
            placeholder={t('topic.quiz.questionPlaceholder')}
            value={question}
            onChange={(event) => setQuestion(event.currentTarget.value)}
            disabled={reachedLimit}
          />
          <Textarea
            label={t('topic.quiz.answer')}
            placeholder={t('topic.quiz.answerPlaceholder')}
            value={answer}
            onChange={(event) => setAnswer(event.currentTarget.value)}
            autosize
            minRows={2}
            maxRows={6}
            disabled={reachedLimit}
          />
          <Group justify="flex-end">
            <Button
              leftSection={<IconPlusFilled size={14} />}
              onClick={handleAdd}
              disabled={!canAdd}
              loading={isCreating}
            >
              {t('topic.quiz.addCard')}
            </Button>
          </Group>
          {reachedLimit && (
            <Text size="xs" c="red">
              {t('topic.quiz.limitReached')}
            </Text>
          )}
        </Stack>
      </Paper>

      {indexCards.length === 0 ? (
        <Text c="dimmed" size="sm" ta="center" py="md">
          {t('topic.quiz.emptyHint')}
        </Text>
      ) : (
        <Stack gap="xs">
          {indexCards.map((card, index) => (
            <Paper key={card.id ?? index} withBorder p="md" radius="md">
              <Group justify="space-between" align="flex-start" wrap="nowrap">
                <Stack gap={4} style={{ flex: 1, minWidth: 0 }}>
                  <Text fw={600} size="sm">
                    {card.question}
                  </Text>
                  <Text size="sm" c="dimmed" style={{ whiteSpace: 'pre-wrap' }}>
                    {card.answer}
                  </Text>
                </Stack>
                <Tooltip label={t('topic.quiz.deleteCard')} withArrow>
                  <ActionIcon
                    variant="subtle"
                    color="red"
                    onClick={() => handleDelete(card.id)}
                    loading={isDeleting}
                    aria-label={t('topic.quiz.deleteCard')}
                  >
                    <IconTrash size={16} />
                  </ActionIcon>
                </Tooltip>
              </Group>
            </Paper>
          ))}
        </Stack>
      )}

      {(onSave || onSaveAndView) && (
        <Group justify="flex-end" mt="md" gap="sm">
          {onSave && (
            <Button onClick={onSave} loading={isSubmitting} size="md" variant="default">
              {t('common.save')}
            </Button>
          )}
          {onSaveAndView && (
            <Button onClick={onSaveAndView} loading={isSubmitting} size="md">
              {t('common.saveAndView')}
            </Button>
          )}
        </Group>
      )}
    </Stack>
  );
};

export default QuizStep;
