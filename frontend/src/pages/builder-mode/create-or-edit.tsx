import { Layout } from '../../components/layout.tsx';
import { Title } from '@mantine/core';
import { useTranslation } from 'react-i18next';


const CreateOrEditTopicPage = () => {

  const {t} = useTranslation();

  return (
    <Layout>
      <Title>{t("routes.createTopic")}</Title>
    </Layout>
  );
}

export default CreateOrEditTopicPage;