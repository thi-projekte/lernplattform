import { useEffect, useState } from 'react';
import { Group, Loader, Stack, Text, Textarea, Title } from '@mantine/core';
import { IconAlertTriangle, IconCheck, IconPencil } from '@tabler/icons-react';
import { useDebouncedValue } from '@mantine/hooks';
import { useTranslation } from 'react-i18next';
import { useQueryTopicNote, useUpdateTopicNoteMutation } from '../../api/note.ts';

interface TopicNotesProps {
  topicId: string;
}

const AUTOSAVE_DELAY = 800;

const TopicNotes = ({ topicId }: TopicNotesProps) => {
  const { t } = useTranslation();
  const { data, isLoading } = useQueryTopicNote(topicId);
  const { mutate, isPending, isError } = useUpdateTopicNoteMutation(topicId);

  // null until the note has loaded, so we don't autosave the placeholder.
  const [content, setContent] = useState<string | null>(null);
  // Last value known to be persisted on the server.
  const [savedContent, setSavedContent] = useState('');
  const [debounced] = useDebouncedValue(content ?? '', AUTOSAVE_DELAY);

  // Initialize the editor from the loaded note once.
  useEffect(() => {
    if (data && content === null) {
      const initial = data.content ?? '';
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setContent(initial);
      setSavedContent(initial);
    }
  }, [data, content]);

  // Autosave whenever the debounced value differs from what's on the server.
  // The backend rejects blank content, so skip empty notes.
  useEffect(() => {
    if (content === null || debounced === savedContent || debounced.trim().length === 0) {
      return;
    }
    mutate(debounced, {
      onSuccess: () => setSavedContent(debounced),
    });
  }, [debounced, content, savedContent, mutate]);

  const isDirty = content !== null && content !== savedContent;

  const status = isPending
    ? { icon: <Loader size={12} />, label: t('topic.notes.saving'), color: 'dimmed' as const }
    : isError
      ? {
          icon: <IconAlertTriangle size={13} />,
          label: t('topic.notes.error'),
          color: 'red' as const,
        }
      : isDirty
        ? {
            icon: <IconPencil size={13} />,
            label: t('topic.notes.unsaved'),
            color: 'dimmed' as const,
          }
        : { icon: <IconCheck size={13} />, label: t('topic.notes.saved'), color: 'teal' as const };

  return (
    <Stack gap="sm" style={{ height: '100%', minHeight: 360 }}>
      <Group justify="space-between" align="center">
        <Title order={4}>{t('topic.notes.title')}</Title>
        {content !== null && (
          <Group gap={6} c={status.color} align="center">
            {status.icon}
            <Text size="xs" c={status.color}>
              {status.label}
            </Text>
          </Group>
        )}
      </Group>

      <Textarea
        flex={1}
        placeholder={t('topic.notes.placeholder')}
        value={content ?? ''}
        onChange={(event) => setContent(event.currentTarget.value)}
        disabled={isLoading}
        styles={{
          root: { flex: 1, display: 'flex', flexDirection: 'column', minHeight: 0 },
          wrapper: { flex: 1, minHeight: 0 },
          input: { height: '100%', minHeight: 240, resize: 'none' },
        }}
      />
    </Stack>
  );
};

export default TopicNotes;
