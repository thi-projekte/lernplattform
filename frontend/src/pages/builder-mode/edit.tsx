import { useTranslation } from 'react-i18next';
import { Layout } from '../../components/layout.tsx';
import { Button, Container, Group, Tabs, Title } from '@mantine/core';
import LoadingWrapper from '../../components/loading-wrapper.tsx';
import { useEffect, useState } from 'react';
import type { Topic } from '../../schemas/topic.ts';
import { useQueryTopic } from '../../api/topic.ts';
import { useParams } from 'react-router';
import CoreDataStep from '../../components/topic/core-data-step.tsx';
import AssociatedTopicsStep from '../../components/topic/associated-topics-step.tsx';
import ContentElementsDnd from '../../components/topic/content-elements-dnd.tsx';


const EditTopicPage = () => {

  const {t} = useTranslation();
  const {topicId} = useParams<{topicId: string}>();

  const [topic, setTopic] = useState<Partial<Topic>>({});

  const {data, isLoading} = useQueryTopic(topicId ?? '', true);

  useEffect(() => {
    if (data && Object.keys(topic).length === 0) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setTopic(data);
    }
  }, [data, topic]);

  return (
    <Layout>
      <Title mb={32}>{t('routes.editTopic')}</Title>
      <Container>
        <LoadingWrapper isLoading={isLoading || !topic.title}>
          <Tabs defaultValue="coreData">
            <Tabs.List mb={16}>
              <Tabs.Tab value="coreData">{t('topic.steps.coreDataTitle')}</Tabs.Tab>
              <Tabs.Tab value="associatedTopics">{t('topic.steps.associatedTopicsTitle')}</Tabs.Tab>
              <Tabs.Tab value="contentElements">{t('topic.steps.contentElementsTitle')}</Tabs.Tab>
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
          </Tabs>
          <Group justify="flex-end" mt="xl">
            <Button>
              {t('common.save')}
            </Button>
          </Group>
        </LoadingWrapper>
      </Container>
    </Layout>
  );
}

export default EditTopicPage;