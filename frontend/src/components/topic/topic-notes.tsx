import { useEffect, useState, type ReactNode } from 'react';
import { Box, Group, Loader, Stack, Text, Title } from '@mantine/core';
import { IconAlertTriangle, IconCheck, IconPencil } from '@tabler/icons-react';
import { useDebouncedValue } from '@mantine/hooks';
import { useTranslation } from 'react-i18next';
import { useQueryTopicNote, useUpdateTopicNoteMutation } from '../../api/note.ts';
import RtfEditor from '../rtf-editor.tsx';

const AUTOSAVE_DELAY = 800;

// TipTap serialises an empty document as "<p></p>"; the backend rejects blank
// content, so treat notes with no text as empty and skip saving them.
const stripHtml = (html: string) =>
  html
    .replace(/<[^>]*>/g, '')
    .replace(/&nbsp;/g, ' ')
    .trim();

interface StatusInfo {
  icon: ReactNode;
  label: string;
  color: 'dimmed' | 'teal' | 'red';
}

interface NotesEditorProps {
  topicId: string;
  initialContent: string;
}

const NotesEditor = ({ topicId, initialContent }: NotesEditorProps) => {
  const { t } = useTranslation();
  const { mutate, isPending, isError } = useUpdateTopicNoteMutation(topicId);

  const [content, setContent] = useState(initialContent);
  const [savedContent, setSavedContent] = useState(initialContent);
  const [debounced] = useDebouncedValue(content, AUTOSAVE_DELAY);

  // Autosave once typing settles and the content actually changed.
  useEffect(() => {
    if (debounced === savedContent || stripHtml(debounced).length === 0) return;
    mutate(debounced, { onSuccess: () => setSavedContent(debounced) });
  }, [debounced, savedContent, mutate]);

  const isDirty = content !== savedContent;

  const status: StatusInfo = isPending
    ? { icon: <Loader size={12} />, label: t('topic.notes.saving'), color: 'dimmed' }
    : isError
      ? { icon: <IconAlertTriangle size={13} />, label: t('topic.notes.error'), color: 'red' }
      : isDirty
        ? { icon: <IconPencil size={13} />, label: t('topic.notes.unsaved'), color: 'dimmed' }
        : { icon: <IconCheck size={13} />, label: t('topic.notes.saved'), color: 'teal' };

  return (
    <Stack gap="sm" style={{ height: '100%', minHeight: 360 }}>
      <Group justify="space-between" align="center">
        <Title order={4}>{t('topic.notes.title')}</Title>
        <Group gap={6} align="center" c={status.color}>
          {status.icon}
          <Text size="xs" c={status.color}>
            {status.label}
          </Text>
        </Group>
      </Group>

      {/* Cap the height so long notes scroll internally instead of growing the
          page forever. The editor toolbar is sticky and stays visible. */}
      <Box style={{ maxHeight: 'calc(100vh - 280px)', overflowY: 'auto' }}>
        <RtfEditor value={initialContent} onChange={setContent} />
      </Box>
    </Stack>
  );
};

interface TopicNotesProps {
  topicId: string;
}

const TopicNotes = ({ topicId }: TopicNotesProps) => {
  const { data, isLoading } = useQueryTopicNote(topicId);

  // Mount the editor only once the note has loaded so its initial content is set.
  if (isLoading || !data) {
    return (
      <Group justify="center" py="xl">
        <Loader size="sm" />
      </Group>
    );
  }

  return <NotesEditor topicId={topicId} initialContent={data.content ?? ''} />;
};

export default TopicNotes;
