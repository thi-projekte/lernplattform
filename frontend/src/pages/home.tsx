import { useUserService } from '../provider/user-provider.tsx';
import { Layout } from '../components/layout.tsx';

const Homepage = () => {
  const userProfile = useUserService();
  return (
    <Layout>
      <p>
        Welcome: {userProfile.account.username} {userProfile.roles.join(', ')}
      </p>
    </Layout>
  );
};

export default Homepage;
