import { useState } from 'react';
import {
  Button,
  Code,
  CopyButton,
  Divider,
  FileInput,
  Group,
  Modal,
  ScrollArea,
  Stack,
  Text,
} from '@mantine/core';
import { IconCheck, IconChevronDown, IconChevronRight, IconCopy } from '@tabler/icons-react';
import { notifications } from '@mantine/notifications';
import { useTranslation } from 'react-i18next';
import { FullImportSchema } from '../../schemas/topic.ts';
import { fullImportSchemaText } from '../../constants/full-import-schema.ts';
import { useImportTopicsMutation } from '../../api/topic.ts';

interface ImportTopicsModalProps {
  opened: boolean;
  onClose: () => void;
}

// The backend returns a helpful plain-text reason on a failed import (e.g.
// "Category not found by ID or title: ..."). Surface it instead of the generic
// message so the user can see what's actually wrong (see issue #331).
const extractBackendError = (error: unknown): string | null => {
  if (typeof error !== 'object' || error === null || !('response' in error)) return null;
  const data = (error as { response?: { data?: unknown } }).response?.data;
  if (typeof data === 'string' && data.trim()) return data.trim();
  if (data && typeof data === 'object' && 'message' in data) {
    const message = (data as { message?: unknown }).message;
    if (typeof message === 'string' && message.trim()) return message.trim();
  }
  return null;
};

const ImportTopicsModal = ({ opened, onClose }: ImportTopicsModalProps) => {
  const { t } = useTranslation();
  const [importFile, setImportFile] = useState<File | null>(null);
  const [importError, setImportError] = useState<string | null>(null);
  const [schemaOpen, setSchemaOpen] = useState(false);
  const { mutate: importTopics, isPending: isImporting } = useImportTopicsMutation();

  const handleClose = () => {
    setImportFile(null);
    setImportError(null);
    onClose();
  };

  const handleSubmit = () => {
    if (!importFile) return;
    const reader = new FileReader();
    reader.onload = (e) => {
      try {
        const raw = JSON.parse(e.target?.result as string);
        const result = FullImportSchema.safeParse(raw);
        if (!result.success) {
          setImportError(result.error.issues[0]?.message ?? t('topic.actions.importJsonError'));
          return;
        }
        importTopics(result.data, {
          onSuccess: () => {
            notifications.show({ color: 'green', message: t('topic.actions.importJsonSuccess') });
            handleClose();
          },
          onError: (error) => {
            setImportError(extractBackendError(error) ?? t('topic.actions.importJsonError'));
          },
        });
      } catch {
        setImportError(t('topic.actions.importJsonError'));
      }
    };
    reader.readAsText(importFile);
  };

  return (
    <Modal
      opened={opened}
      onClose={handleClose}
      title={t('topic.actions.importJson')}
      centered
      size="lg"
    >
      <Stack gap="md">
        <FileInput
          label="JSON"
          accept=".json,application/json"
          value={importFile}
          onChange={(f) => {
            setImportFile(f);
            setImportError(null);
          }}
          placeholder="topics.json"
          error={importError}
        />
        <Button fullWidth disabled={!importFile} loading={isImporting} onClick={handleSubmit}>
          {t('topic.actions.importJsonSubmit')}
        </Button>

        <Divider />

        <Stack gap="xs">
          <Text size="sm" c="dimmed">
            {t('topic.actions.importJsonSchemaHint')}
          </Text>
          <Group justify="space-between" wrap="nowrap">
            <Button
              variant="subtle"
              size="compact-sm"
              leftSection={
                schemaOpen ? <IconChevronDown size={16} /> : <IconChevronRight size={16} />
              }
              onClick={() => setSchemaOpen((open) => !open)}
            >
              {t('topic.actions.importJsonSchema')}
            </Button>
            <CopyButton value={fullImportSchemaText} timeout={2000}>
              {({ copied, copy }) => (
                <Button
                  variant="light"
                  size="compact-sm"
                  color={copied ? 'teal' : 'blue'}
                  leftSection={copied ? <IconCheck size={16} /> : <IconCopy size={16} />}
                  onClick={copy}
                >
                  {copied ? t('common.copied') : t('common.copy')}
                </Button>
              )}
            </CopyButton>
          </Group>
          {schemaOpen && (
            <ScrollArea h={320} type="auto">
              <Code block>{fullImportSchemaText}</Code>
            </ScrollArea>
          )}
        </Stack>
      </Stack>
    </Modal>
  );
};

export default ImportTopicsModal;
