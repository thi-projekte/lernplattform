import { Layout } from '../../components/layout.tsx';
import { Title } from '@mantine/core';
import { useTranslation } from 'react-i18next';
import { useState } from 'react';
import { type Topic, TopicCoreDataSchema } from '../../schemas/topic.ts';
import StepperProgress, { type StepperStep } from '../../components/stepper-progress.tsx';
import CoreDataStep from '../../components/topic/core-data-step.tsx';


const CreateOrEditTopicPage = () => {

  const {t} = useTranslation();

  const [topic, setTopic] = useState<Partial<Topic>>({});

  const steps: StepperStep[] = [
    {
      label: t('topic.steps.coreDataTitle'),
      description: t('topic.steps.coreDataDescription'),
      canProceed: TopicCoreDataSchema.safeParse(topic).success ?? false,
      step: (
        <CoreDataStep topic={topic} setTopic={setTopic} />
      )
    }
  ];


  return (
    <Layout>
      <Title mb={32}>{t("routes.createTopic")}</Title>
      <StepperProgress steps={steps} />
    </Layout>
  );
}

export default CreateOrEditTopicPage;