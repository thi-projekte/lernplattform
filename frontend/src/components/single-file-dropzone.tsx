import { useState } from 'react';
import { Dropzone, type FileWithPath } from '@mantine/dropzone';
import { Group, rem, Text, ActionIcon, Paper, Stack } from '@mantine/core';
import { IconPhoto, IconUpload, IconX, IconFileCheck } from '@tabler/icons-react';
import { useTranslation } from 'react-i18next';

interface FileDropProps {
  acceptedTypes: string[];
  onDrop: (files: FileWithPath[]) => void;
  loading?: boolean;
  maxFileSize?: number;
}

const SingleFileDropzone = ({ acceptedTypes, onDrop, loading, maxFileSize }: FileDropProps) => {
  const { t } = useTranslation();
  // Keep track of the selected file locally to show the "Selected" state
  const [selectedFile, setSelectedFile] = useState<FileWithPath | null>(null);

  const handleDrop = (files: FileWithPath[]) => {
    const file = files[0];
    if (file) {
      setSelectedFile(file);
      onDrop(files);
    }
  };

  const handleRemove = () => {
    setSelectedFile(null);
    // Optionally call onDrop with an empty array if your parent needs to know it's cleared
    onDrop([]);
  };

  // If a file is selected, show the preview/remove UI
  if (selectedFile) {
    return (
      <Paper withBorder p="md" radius="md">
        <Group justify="space-between">
          <Group>
            <IconFileCheck size={rem(28)} color="var(--mantine-color-blue-6)" />
            <div>
              <Text size="sm" fw={500} c="bright">
                {selectedFile.name}
              </Text>
              <Text size="xs" c="dimmed">
                {(selectedFile.size / 1024).toFixed(2)} KB
              </Text>
            </div>
          </Group>
          <ActionIcon variant="subtle" color="red" onClick={handleRemove}>
            <IconX style={{ width: rem(20), height: rem(20) }} />
          </ActionIcon>
        </Group>
      </Paper>
    );
  }

  // Otherwise, show the original Dropzone
  return (
    <Dropzone
      onDrop={handleDrop}
      onReject={(files) => console.error('rejected files', files)}
      maxSize={maxFileSize ?? 5 * 1024 ** 2}
      accept={acceptedTypes}
      multiple={false}
      loading={loading}
    >
      <Group justify="center" gap="xl" mih={220} style={{ pointerEvents: 'none' }}>
        <Dropzone.Accept>
          <IconUpload
            style={{ width: rem(52), height: rem(52), color: 'var(--mantine-color-blue-6)' }}
            stroke={1.5}
          />
        </Dropzone.Accept>
        <Dropzone.Reject>
          <IconX
            style={{ width: rem(52), height: rem(52), color: 'var(--mantine-color-red-6)' }}
            stroke={1.5}
          />
        </Dropzone.Reject>
        <Dropzone.Idle>
          <IconPhoto
            style={{ width: rem(52), height: rem(52), color: 'var(--mantine-color-dimmed)' }}
            stroke={1.5}
          />
        </Dropzone.Idle>
        <Stack gap={4} align="center">
          <Text size="xl" inline>
            {t('common.dropYourFileHere')}
          </Text>
          <Text size="sm" c="dimmed" inline mt={7}>
            {t('common.fileSizeLimit', { size: '5MB' })}
          </Text>
        </Stack>
      </Group>
    </Dropzone>
  );
};

export default SingleFileDropzone;
