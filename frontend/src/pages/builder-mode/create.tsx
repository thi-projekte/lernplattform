import { Layout } from '../../components/layout.tsx';
import { Container, Title } from '@mantine/core';
import { useTranslation } from 'react-i18next';
import { useState } from 'react';
import {
  type Topic,
  TopicAssociatedTopicsSchema,
  TopicContentElementsSchema,
  TopicCoreDataSchema,
} from '../../schemas/topic.ts';
import StepperProgress, { type StepperStep } from '../../components/stepper-progress.tsx';
import CoreDataStep from '../../components/topic/core-data-step.tsx';
import AssociatedTopicsStep from '../../components/topic/associated-topics-step.tsx';
import ContentElementsDnd from '../../components/topic/content-elements-dnd.tsx';
import { useCreateTopicMutation } from '../../api/topic.ts';
import { useNavigate } from 'react-router';

const CreateTopicPage = () => {
  const { t } = useTranslation();
  const { isPending, mutateAsync } = useCreateTopicMutation();
  const navigate = useNavigate();

  const [topic, setTopic] = useState<Partial<Topic>>({ contentElements: [] });

  const steps: StepperStep[] = [
    {
      label: t('topic.steps.coreDataTitle'),
      canProceed: TopicCoreDataSchema.safeParse(topic).success ?? false,
      step: <CoreDataStep topic={topic} setTopic={setTopic} />,
    },
    {
      label: t('topic.steps.associatedTopicsTitle'),
      canProceed: TopicAssociatedTopicsSchema.safeParse(topic).success ?? false,
      step: <AssociatedTopicsStep topic={topic} setTopic={setTopic} />,
    },
    {
      label: t('topic.steps.contentElementsTitle'),
      canProceed: TopicContentElementsSchema.safeParse(topic).success ?? false,
      step: <ContentElementsDnd topic={topic} setTopic={setTopic} />,
    },
  ];

  const onComplete = async () => {
    const result = await mutateAsync(topic);
    console.log(result);
    navigate('/builder-mode');
  };

  return (
    <Layout>
      <Title mb={32}>{t('routes.createTopic')}</Title>
      <Container fluid px="md">
        <StepperProgress
          steps={steps}
          onComplete={onComplete}
          isLoading={isPending}
          lastStepLabel={t('topic.actions.create')}
        />
      </Container>
    </Layout>
  );
};

export default CreateTopicPage;
