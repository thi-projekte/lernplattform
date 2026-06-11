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
import ContentElementsDnd from '../../components/topic/content-elements-dnd.tsx';
import QuizStep from '../../components/topic/quiz-step.tsx';
import { useCreateTopicMutation } from '../../api/topic.ts';
import { useNavigate } from 'react-router';
import { track } from '@plausible-analytics/tracker';

const CreateTopicPage = () => {
  const { t } = useTranslation();
  const { isPending, mutateAsync } = useCreateTopicMutation();
  const navigate = useNavigate();

  const [topic, setTopic] = useState<Partial<Topic>>({ contentElements: [], indexCards: [] });

  const submit = async (viewAfter: boolean) => {
    const created = await mutateAsync(topic);
    track('topicCreation', { props: { topicTitle: topic.title ?? '' } });
    if (viewAfter && created?.id) {
      navigate(`/topics/${created.id}/details`);
    } else {
      navigate('/builder-mode');
    }
  };

  const onComplete = () => submit(false);

  const steps: StepperStep[] = [
    {
      label: t('topic.steps.coreDataTitle'),
      canProceed: TopicCoreDataSchema.safeParse(topic).success,
      step: <CoreDataStep topic={topic} setTopic={setTopic} />,
    },
    {
      label: t('topic.steps.associatedTopicsTitle'),
      canProceed: TopicAssociatedTopicsSchema.safeParse(topic).success,
      step: <AssociatedTopicsStep topic={topic} setTopic={setTopic} />,
    },
    {
      label: t('topic.steps.contentElementsTitle'),
      // At least one content element is required before moving to Quiz.
      canProceed: (topic.contentElements ?? []).length >= 1,
      step: <ContentElementsDnd topic={topic} setTopic={setTopic} />,
    },
    {
      label: t('topic.steps.quizTitle'),
      canProceed: Array.isArray(topic.indexCards) && (topic.indexCards ?? []).length <= 20,
      step: (
        <QuizStep
          topic={topic}
          setTopic={setTopic}
          onSave={() => submit(false)}
          onSaveAndView={() => submit(true)}
          isSubmitting={isPending}
        />
      ),
      hideFooter: true,
    },
  ];

  return (
    <Layout>
      <Title mb={32}>{t('routes.createTopic')}</Title>
      <Container fluid px="md">
        <StepperProgress
          steps={steps}
          onComplete={onComplete}
          onBack={() => navigate('/builder-mode')}
          isLoading={isPending}
          lastStepLabel={t('topic.actions.create')}
        />
      </Container>
    </Layout>
  );
};

export default CreateTopicPage;
