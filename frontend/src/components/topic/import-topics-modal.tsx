import { useState } from 'react';
import { Button, FileInput, Modal, Stack } from '@mantine/core';
import { notifications } from '@mantine/notifications';
import { useTranslation } from 'react-i18next';
import { FullImportSchema } from '../../schemas/topic.ts';
import { useImportTopicsMutation } from '../../api/topic.ts';

interface ImportTopicsModalProps {
  opened: boolean;
  onClose: () => void;
}

const ImportTopicsModal = ({ opened, onClose }: ImportTopicsModalProps) => {
  const { t } = useTranslation();
  const [importFile, setImportFile] = useState<File | null>(null);
  const [importError, setImportError] = useState<string | null>(null);
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
          onError: () => {
            setImportError(t('topic.actions.importJsonError'));
          },
        });
      } catch {
        setImportError(t('topic.actions.importJsonError'));
      }
    };
    reader.readAsText(importFile);
  };

  return (
    <Modal opened={opened} onClose={handleClose} title={t('topic.actions.importJson')} centered>
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
      </Stack>
    </Modal>
  );
};

export default ImportTopicsModal;
