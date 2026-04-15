import { type Topic, TopicCoreDataSchema } from '../../schemas/topic.ts';
import { schemaResolver, useForm } from '@mantine/form';
import { NumberInput, Textarea, TextInput } from '@mantine/core';
import { useTranslation } from 'react-i18next';

interface TopicCoreDataStepProps {
  topic: Partial<Topic>;
  setTopic: (topic: Partial<Topic>) => void;
}

const TopicCoreDataStep = ({topic, setTopic}: TopicCoreDataStepProps) => {

  const {t} = useTranslation();

  const form = useForm({
    mode: 'uncontrolled',
    initialValues: {
      title: topic.title,
      teaser: topic.teaser,
      categories: topic.categories,
      estimatedLearningDuration: topic.estimatedLearningDuration
    },
    validate: schemaResolver(TopicCoreDataSchema, {sync: true})
  });

  const onSubmit = form.onSubmit((values) => {
    setTopic({
      ...topic,
      ...values
    });
  });

  return (
    <form onSubmit={onSubmit}>
      <TextInput
        label={t("topic.fields.title")}
        withAsterisk
        key={form.key('title')}
        {...form.getInputProps('title')}
      />
      <Textarea
        label={t("topic.fields.teaser")}
        withAsterisk
        key={form.key('teaser')}
        {...form.getInputProps('teaser')}
      />
      <NumberInput
        label={t("topic.fields.estimatedLearningDuration")}
        withAsterisk
        key={form.key('estimatedLearningDuration')}
        {...form.getInputProps('estimatedLearningDuration')}
      />
    </form>
  )

}

export default TopicCoreDataStep;