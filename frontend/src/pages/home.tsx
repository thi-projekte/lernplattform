import {useUserProfile} from "../provider/user-provider.tsx";


const Homepage = () => {
    const userProfile = useUserProfile();


    return (
        <p>Welcome: {userProfile.username}</p>
    )
}

export default Homepage;