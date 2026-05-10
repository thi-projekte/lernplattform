import {
  IconBrandSpotify,
  IconBrandYoutube,
  IconFileDescription,
  IconHeadphones,
  IconLink,
  IconPhoto,
  IconVideo,
} from '@tabler/icons-react';
import type { ContentElementType } from '../../schemas/content-element';
import HexagonNode from './hexagon-node';
import type { NodeProps, Node } from '@xyflow/react';
import type { GraphContentNodeData } from './topic-graph.types';
import { CONTENT_ICONS } from '../icon-picker/icons';

type ContentNodeProps = NodeProps<Node<GraphContentNodeData>>;

const getIconForType = (type: ContentElementType) => {
  switch (type) {
    case 'PDF':
      return IconFileDescription;
    case 'VIDEO_FILE':
      return IconVideo;
    case 'YOUTUBE_LINK':
      return IconBrandYoutube;
    case 'AUDIO_FILE':
      return IconHeadphones;
    case 'SPOTIFY_LINK':
      return IconBrandSpotify;
    case 'IMAGE':
      return IconPhoto;
    case 'URI':
      return IconLink;
    default:
      return IconFileDescription;
  }
};

const ContentNode = ({ data, ...props }: ContentNodeProps) => {
  const userIcon = data.payload.icon ? CONTENT_ICONS[data.payload.icon] : undefined;
  return (
    <HexagonNode
      Icon={userIcon ?? getIconForType(data.payload.type)}
      label={data.title}
      color="#1c7ed6"
      size={56}
      labelSize="xs"
      labelFontWeight={600}
      {...props}
    />
  );
};

export default ContentNode;
