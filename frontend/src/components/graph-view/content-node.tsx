import { IconBrandSpotifyFilled, IconBrandYoutubeFilled, IconFileDescription, IconHeadphonesFilled, IconLink, IconPhoto, IconVideo } from "@tabler/icons-react";
import type { AnyContentElementDto, ContentElementType } from "../../schemas/content-element";
import HexagonNode from "./hexagon-node";
import type { NodeProps } from "@xyflow/react";


interface ContentNodeProps extends NodeProps {
    contentElement: AnyContentElementDto;
}

const getIconForType = (type: ContentElementType) => {
    switch (type) {
        case 'PDF':
            return IconFileDescription;
        case 'VIDEO_FILE':
            return IconVideo;
        case 'YOUTUBE_LINK':
            return IconBrandYoutubeFilled;
        case 'AUDIO_FILE':
            return IconHeadphonesFilled;
        case 'SPOTIFY_LINK':
            return IconBrandSpotifyFilled;
        case 'IMAGE':
            return IconPhoto;
        case 'URI':
            return IconLink;
        default:
            return IconFileDescription;
    }
};

const ContentNode = ({ contentElement, ...props }: ContentNodeProps) => {

    return <HexagonNode Icon={getIconForType(contentElement.type)} label={contentElement.title} color="#1c7ed6" iconSize={24} labelSize="sm" {...props} />;
};

export default ContentNode;