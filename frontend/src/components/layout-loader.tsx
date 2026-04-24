import { Center, Loader } from '@mantine/core';
import { Layout } from './layout';

const LayoutLoader = () => (
  <Layout>
    <Center h="100vh">
      <Loader />
    </Center>
  </Layout>
);

export default LayoutLoader;
