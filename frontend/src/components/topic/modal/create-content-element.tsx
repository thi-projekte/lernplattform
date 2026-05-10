import { Alert, Button, Group, Modal, Stack, TextInput } from '@mantine/core';
import { IconAlertCircle } from '@tabler/icons-react';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import {
  type AnyContentElementDto,
  type AnyContentElementRequest,
  AudioFileElementRequestSchema,
  type ContentElementType,
  ImageElementRequestSchema,
  PdfElementRequestSchema,
  RtfElementRequestSchema,
  SpotifyLinkElementRequestSchema,
  UriElementRequestSchema,
  VideoFileElementRequestSchema,
  YouTubeLinkElementRequestSchema,
} from '../../../schemas/content-element.ts';
import ContentElementTypeSelect from '../content-element-type-select.tsx';
import SingleFileDropzone from '../../single-file-dropzone.tsx';
import { type FileWithPath, MIME_TYPES } from '@mantine/dropzone';
import UriInput from '../../uri-input.tsx';
import RtfEditor from '../../rtf-editor.tsx';
import type { ZodObject } from 'zod';
import { useForm } from '@mantine/form';
import { useCreateContentElementMutation } from '../../../api/topic.ts';
import IconPicker from '../../icon-picker/icon-picker.tsx';
import { DEFAULT_ICON_BY_TYPE } from '../../icon-picker/icons.ts';

interface CreateContentElementModalProps {
  opened: boolean;
  onClose: () => void;
  onAddContentElement: (contentElement: AnyContentElementDto) => void;
}

const typesWithFile: ContentElementType[] = ['AUDIO_FILE', 'PDF', 'VIDEO_FILE', 'IMAGE'];
const typesWithUri: ContentElementType[] = ['URI', 'SPOTIFY_LINK', 'YOUTUBE_LINK'];
const typesWithRtfEditor: ContentElementType[] = ['RTF'];
const MB = 1024 ** 2;

/* eslint-disable-next-line */
export const allowedFileTypes: Partial<Record<ContentElementType, string[]>> = {
  AUDIO_FILE: ['audio/mpeg', 'audio/wav', 'audio/ogg', 'audio/aac'],
  PDF: [MIME_TYPES.pdf],
  VIDEO_FILE: [MIME_TYPES.mp4, 'video/quicktime', 'video/x-msvideo'],
  IMAGE: [MIME_TYPES.png, MIME_TYPES.jpeg, MIME_TYPES.svg, MIME_TYPES.gif],
};

const maxFileSizes: Partial<Record<ContentElementType, number>> = {
  PDF: 10 * MB,
  AUDIO_FILE: 20 * MB,
  IMAGE: 10 * MB,
  VIDEO_FILE: 100 * MB,
};

const requestValidatorMapping: Record<ContentElementType, ZodObject> = {
  PDF: PdfElementRequestSchema,
  VIDEO_FILE: VideoFileElementRequestSchema,
  AUDIO_FILE: AudioFileElementRequestSchema,
  YOUTUBE_LINK: YouTubeLinkElementRequestSchema,
  SPOTIFY_LINK: SpotifyLinkElementRequestSchema,
  RTF: RtfElementRequestSchema,
  URI: UriElementRequestSchema,
  IMAGE: ImageElementRequestSchema,
};

const CreateContentElementModal = ({
  opened,
  onClose,
  onAddContentElement,
}: CreateContentElementModalProps) => {
  const { t } = useTranslation();
  const [hasSubmitAttempted, setHasSubmitAttempted] = useState(false);

  const { isPending, mutateAsync } = useCreateContentElementMutation();

  const form = useForm({
    initialValues: {
      title: '',
      type: null as ContentElementType | null,
      icon: '',
      file: null as FileWithPath | null,
      originalFileName: '',
      uri: '',
      rtfText: '',
    },
    validate: (values) => {
      if (!values.type) return { type: t('common.shouldNotBeEmpty') };
      const schema = requestValidatorMapping[values.type];
      const result = schema.safeParse(values);
      return result.success ? {} : result.error.flatten().fieldErrors;
    },
  });

  const submit = form.onSubmit(
    async (values) => {
      const contentElement = await mutateAsync({
        request: values as AnyContentElementRequest,
        file: values.file as unknown as File,
      });

      onAddContentElement(contentElement);
      form.reset();
      setHasSubmitAttempted(false);
      onClose();
    },
    () => setHasSubmitAttempted(true)
  );

  const handleClose = () => {
    setHasSubmitAttempted(false);
    onClose();
  };

  return (
    <Modal
      opened={opened}
      onClose={handleClose}
      title={t('topic.headings.createContentElement')}
      size="xl"
    >
      <form onSubmit={submit}>
        <Stack>
          {hasSubmitAttempted && Object.keys(form.errors).length > 0 && (
            <Alert color="red" variant="light" icon={<IconAlertCircle size={18} />}>
              {t('topic.contentElementForm.incompleteFormError')}
            </Alert>
          )}
          <TextInput
            label={t('topic.fields.contentElementTitle')}
            withAsterisk
            {...form.getInputProps('title')}
          />
          <ContentElementTypeSelect
            {...form.getInputProps('type')}
            onChange={(val) => {
              form.setFieldValue('type', val);
              form.setFieldValue('icon', val ? DEFAULT_ICON_BY_TYPE[val] : '');
              form.setFieldValue('file', null);
              form.setFieldValue('uri', '');
              form.setFieldValue('rtfText', '');
              form.clearFieldError('file');
            }}
            required
          />
          {form.values.type && (
            <IconPicker
              label={t('topic.fields.icon')}
              value={form.values.icon || null}
              onChange={(name) => form.setFieldValue('icon', name)}
              error={form.errors.icon}
              required
            />
          )}
          {form.values.type && typesWithFile.includes(form.values.type) && (
            <SingleFileDropzone
              acceptedTypes={allowedFileTypes[form.values.type] ?? []}
              maxFileSize={maxFileSizes[form.values.type]}
              error={form.errors.file}
              onDrop={(files) => {
                form.setFieldValue('file', files.length > 0 ? files[0] : null);
                form.setFieldValue('originalFileName', files.length > 0 ? files[0].name : '');
                form.clearFieldError('file');
              }}
              onReject={(message) => {
                form.setFieldValue('file', null);
                form.setFieldValue('originalFileName', '');
                form.setFieldError('file', message);
              }}
            />
          )}
          {form.values.type && typesWithUri.includes(form.values.type) && (
            <UriInput {...form.getInputProps('uri')} />
          )}
          {form.values.type && typesWithRtfEditor.includes(form.values.type) && (
            <RtfEditor {...form.getInputProps('rtfText')} />
          )}

          <Group justify="flex-end" mt="xl">
            <Button variant="outline" onClick={handleClose} disabled={isPending}>
              {t('common.cancel')}
            </Button>
            <Button type="submit" loading={isPending}>
              {t('common.create')}
            </Button>
          </Group>
        </Stack>
      </form>
    </Modal>
  );
};

export default CreateContentElementModal;
