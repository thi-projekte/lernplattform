import {useUserService} from '../provider/user-provider.tsx';

const Homepage = () => {
  const userProfile = useUserService();
  return <p>Welcome: {userProfile.account.username} {userProfile.roles.join(', ')}</p>;
};

export default Homepage;
