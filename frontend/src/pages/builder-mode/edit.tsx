import { useTranslation } from 'react-i18next';
import { Layout } from '../../components/layout.tsx';
import { Button, Container, Group, Tabs, Title } from '@mantine/core';
import { useEffect, useState } from 'react';
import { type Topic, TopicSchema } from '../../schemas/topic.ts';
import { useEditTopicMutation, useQueryTopic } from '../../api/topic.ts';
import { useNavigate, useParams } from 'react-router';
import CoreDataStep from '../../components/topic/core-data-step.tsx';
import AssociatedTopicsStep from '../../components/topic/associated-topics-step.tsx';
import ContentElementsDnd from '../../components/topic/content-elements-dnd.tsx';
import QuizStep from '../../components/topic/quiz-step.tsx';
import { notifications } from '@mantine/notifications';
import LayoutLoader from '../../components/layout-loader.tsx';
import { useUserService } from '../../provider/user-provider.tsx';

const EditTopicPage = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { topicId } = useParams<{ topicId: string }>();
  const userService = useUserService();
  const currentUsername = userService.account.username?.toLowerCase();

  const [topic, setTopic] = useState<Partial<Topic>>({});

  const { data, isLoading } = useQueryTopic(topicId ?? '', true);

  const isOwner = !!data && !!currentUsername && data.creatorId?.toLowerCase() === currentUsername;

  const { mutateAsync, isPending } = useEditTopicMutation(topicId ?? '');

  const canSave = TopicSchema.safeParse(topic).success;

  useEffect(() => {
    if (data && Object.keys(topic).length === 0) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setTopic(data);
    }
  }, [data, topic]);

  useEffect(() => {
    if (data && currentUsername && !isOwner) {
      navigate(`/topics/${topicId}/details`, { replace: true });
    }
  }, [data, currentUsername, isOwner, navigate, topicId]);

  const saveChanges = async (saveAndView: boolean) => {
    if (!canSave) return;
    const result = await mutateAsync(topic);
    if (result.status < 204) {
      notifications.show({
        title: t('common.success'),
        message: t('topic.other.successfullySavedTopic'),
        color: 'green',
      });

      if (saveAndView) {
        navigate(`/topics/${topicId}/details`);
      }
    } else {
      notifications.show({
        message: t('common.serverError'),
      });
    }
  };

  if (isLoading || topic.title === undefined) {
    return <LayoutLoader />;
  }

  return (
    <Layout>
      <Title mb={32}>{t('routes.editTopic')}</Title>
      <Container fluid px="md">
        <Tabs defaultValue="coreData">
          <Tabs.List mb={16} grow>
            <Tabs.Tab value="coreData">{t('topic.steps.coreDataTitle')}</Tabs.Tab>
            <Tabs.Tab value="associatedTopics">{t('topic.steps.associatedTopicsTitle')}</Tabs.Tab>
            <Tabs.Tab value="contentElements">{t('topic.steps.contentElementsTitle')}</Tabs.Tab>
            <Tabs.Tab value="quiz">{t('topic.steps.quizTitle')}</Tabs.Tab>
          </Tabs.List>
          <Tabs.Panel value="coreData">
            <CoreDataStep topic={topic} setTopic={setTopic} />
          </Tabs.Panel>
          <Tabs.Panel value="associatedTopics">
            <AssociatedTopicsStep topic={topic} setTopic={setTopic} />
          </Tabs.Panel>
          <Tabs.Panel value="contentElements">
            <ContentElementsDnd topic={topic} setTopic={setTopic} />
          </Tabs.Panel>
          <Tabs.Panel value="quiz">
            <QuizStep topic={topic} setTopic={setTopic} />
          </Tabs.Panel>
        </Tabs>
        <Group justify="flex-end" mt="xl">
          <Button
            loading={isPending}
            type="button"
            onClick={() => saveChanges(false)}
            disabled={!canSave}
          >
            {t('common.save')}
          </Button>
          <Button
            loading={isPending}
            type="button"
            variant="outline"
            onClick={() => saveChanges(true)}
            disabled={!canSave}
          >
            {t('common.saveAndView')}
          </Button>
        </Group>
      </Container>
    </Layout>
  );
};

export default EditTopicPage;
