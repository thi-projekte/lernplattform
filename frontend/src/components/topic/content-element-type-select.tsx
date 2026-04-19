import { useTranslation } from 'react-i18next';
import {
  type ContentElementType,
  ContentElementTypeSchema,
} from '../../schemas/content-element.ts';
import { Select } from '@mantine/core';

interface ContentElementTypeSelectProps {
  value?: ContentElementType;
  onChange: (val: ContentElementType | null) => void;
  required?: boolean;
  error?: string;
}

const ContentElementTypeSelect = ({value, onChange, error, required}: ContentElementTypeSelectProps) => {
  const { t } = useTranslation();

  const data = ContentElementTypeSchema.options.map((type) => ({
    value: type,
    label: t(`topic.contentElementType.${type}`),
  }));

  return (
    <Select
      label={t('topic.contentElementType.label')}
      placeholder={t('topic.contentElementType.placeholder')}
      data={data}
      value={value}
      onChange={onChange}
      clearable
      error={error}
      withAsterisk={required}
    />
  );
}

export default ContentElementTypeSelect;