import LiteYouTubeEmbed from 'react-lite-youtube-embed';
import 'react-lite-youtube-embed/dist/LiteYouTubeEmbed.css';

interface YouTubeEmbedProps {
  url: string;
}

const YouTubeEmbed = ({ url }: YouTubeEmbedProps) => {
  const getID = (url: string) => {
    const regExp = /^.*(youtu.be\/|v\/|u\/\w\/|embed\/|watch\?v=|&v=)([^#&?]*).*/;
    const match = url.match(regExp);
    return match && match[2].length === 11 ? match[2] : null;
  };

  const videoId = getID(url);

  if (!videoId) return <p>Invalid YouTube Link</p>;

  return <LiteYouTubeEmbed id={videoId} title="" />;
};

export default YouTubeEmbed;
