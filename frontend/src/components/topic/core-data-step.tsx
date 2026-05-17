import { type Topic, TopicCoreDataSchema } from '../../schemas/topic.ts';
import { schemaResolver, useForm } from '@mantine/form';
import { NumberInput, Textarea, TextInput } from '@mantine/core';
import { useTranslation } from 'react-i18next';
import CategorySelect from './category-select.tsx';
import type { Dispatch, SetStateAction } from 'react';

interface TopicCoreDataStepProps {
  topic: Partial<Topic>;
  setTopic: Dispatch<SetStateAction<Partial<Topic>>>;
}

const TopicCoreDataStep = ({ topic, setTopic }: TopicCoreDataStepProps) => {
  const { t } = useTranslation();

  const form = useForm({
    mode: 'controlled',
    initialValues: {
      title: topic.title,
      teaser: topic.teaser,
      categories: topic.categories ?? [],
      estimatedLearningDuration: topic.estimatedLearningDuration,
    },
    validate: schemaResolver(TopicCoreDataSchema, { sync: true }),
    onValuesChange: (values) =>
      form.validate() && form.isValid() && setTopic((prev) => ({ ...prev, ...values })),
  });

  return (
    <>
      <TextInput
        label={t('topic.fields.title')}
        withAsterisk
        key={form.key('title')}
        {...form.getInputProps('title')}
      />
      <Textarea
        label={t('topic.fields.teaser')}
        withAsterisk
        key={form.key('teaser')}
        maxLength={512}
        {...form.getInputProps('teaser')}
      />
      <CategorySelect key={form.key('categories')} {...form.getInputProps('categories')} />
      <NumberInput
        label={t('topic.fields.estimatedLearningDuration')}
        suffix={` ${t('topic.fields.estimatedLearningDurationSuffix')}`}
        withAsterisk
        key={form.key('estimatedLearningDuration')}
        {...form.getInputProps('estimatedLearningDuration')}
      />
    </>
  );
};

export default TopicCoreDataStep;
