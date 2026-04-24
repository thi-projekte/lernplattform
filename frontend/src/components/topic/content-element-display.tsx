import type {
  AnyContentElementDto,
  AudioFileElementDto,
  ImageElementDto,
  PdfElementDto,
  RtfElementDto,
  SpotifyLinkElementDto,
  UriElementDto,
  VideoFileElementDto,
  YouTubeLinkElementDto,
} from '../../schemas/content-element.ts';
import YouTubeEmbed from './content/youtube-embed.tsx';
import { AspectRatio, Button, Group, Image, Text } from '@mantine/core';
import { IconDownload, IconFileTypePdf } from '@tabler/icons-react';
import { useTranslation } from 'react-i18next';
import SpotifyEmbed from './content/spotify-embed.tsx';

interface ContentElementDisplayProps {
  contentElement: AnyContentElementDto;
}

const ContentElementDisplay = ({ contentElement }: ContentElementDisplayProps) => {
  const { t } = useTranslation();

  if (contentElement.type === 'RTF') {
    return (
      <div
        dangerouslySetInnerHTML={{
          __html: (contentElement as RtfElementDto).rtfText,
        }}
      />
    );
  }

  if (contentElement.type === 'SPOTIFY_LINK') {
    return <SpotifyEmbed link={(contentElement as SpotifyLinkElementDto).uri} />;
  }

  if (contentElement.type === 'YOUTUBE_LINK') {
    return <YouTubeEmbed url={(contentElement as YouTubeLinkElementDto).uri} />;
  }

  if (contentElement.type === 'URI') {
    return (
      <Text>
        <a href={(contentElement as UriElementDto).uri} target="_blank" rel="noopener noreferrer">
          {contentElement.title}
        </a>
      </Text>
    );
  }

  if (contentElement.type === 'PDF') {
    const pdf = contentElement as PdfElementDto;
    return (
      <Group justify="space-between">
        <Group>
          <IconFileTypePdf size={32} color="red" />
          <div>
            <Text size="sm" fw={500}>
              {pdf.title || 'Document.pdf'}
            </Text>
          </div>
        </Group>
        <Button
          component="a"
          href={pdf.presignedUrl}
          target="_blank"
          download
          leftSection={<IconDownload size={16} />}
          variant="light"
        >
          {t('common.download')}
        </Button>
      </Group>
    );
  }

  if (contentElement.type === 'AUDIO_FILE') {
    const audio = contentElement as AudioFileElementDto;
    return (
      <div style={{ width: '100%', padding: '10px 0' }}>
        <Text size="sm" fw={500} mb={5}>
          {audio.title}
        </Text>
        <audio controls style={{ width: '100%' }}>
          <source src={audio.presignedUrl} />
          Your browser does not support the audio element.
        </audio>
      </div>
    );
  }

  if (contentElement.type === 'VIDEO_FILE') {
    const video = contentElement as VideoFileElementDto;
    return (
      <AspectRatio ratio={16 / 9}>
        <video controls style={{ borderRadius: '8px', width: '100%' }}>
          <source src={video.presignedUrl} />
          Your browser does not support the video tag.
        </video>
      </AspectRatio>
    );
  }

  if (contentElement.type === 'IMAGE') {
    const img = contentElement as ImageElementDto;
    return <Image src={img.presignedUrl} alt={img.title || 'Content Image'} radius="md" />;
  }
};

export default ContentElementDisplay;
