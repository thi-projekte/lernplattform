import { Layout } from '../../components/layout.tsx';
import { Container, Title } from '@mantine/core';
import { useTranslation } from 'react-i18next';
import { useState } from 'react';
import {
  type Topic,
  TopicAssociatedTopicsSchema,
  TopicCoreDataSchema,
} from '../../schemas/topic.ts';
import StepperProgress, { type StepperStep } from '../../components/stepper-progress.tsx';
import CoreDataStep from '../../components/topic/core-data-step.tsx';
import AssociatedTopicsStep from '../../components/topic/associated-topics-step.tsx';

const CreateOrEditTopicPage = () => {
  const { t } = useTranslation();

  const [topic, setTopic] = useState<Partial<Topic>>({});

  const steps: StepperStep[] = [
    {
      label: t('topic.steps.coreDataTitle'),
      description: t('topic.steps.coreDataDescription'),
      canProceed: TopicCoreDataSchema.safeParse(topic).success ?? false,
      step: <CoreDataStep topic={topic} setTopic={setTopic} />,
    },
    {
      label: t('topic.steps.associatedTopicsTitle'),
      description: t('topic.steps.associatedTopicsDescription'),
      canProceed: TopicAssociatedTopicsSchema.safeParse(topic).success ?? false,
      step: <AssociatedTopicsStep topic={topic} setTopic={setTopic}  />,
    },
  ];

  return (
    <Layout>
      <Title mb={32}>{t('routes.createTopic')}</Title>
      <Container>
        <StepperProgress steps={steps} />
      </Container>
    </Layout>
  );
};

export default CreateOrEditTopicPage;
