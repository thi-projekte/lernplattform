import { Badge, Title } from "@mantine/core"
import type { AnyContentElementDto } from "../../../schemas/content-element"
import ContentElementDisplay from "../../topic/content-element-display";
import { useTranslation } from "react-i18next";

interface ContentSidebarContentProps {
    selectedElement: AnyContentElementDto;
}

const ContentSidebarContent = ({ selectedElement }: ContentSidebarContentProps) => {

    const { t } = useTranslation();

    return (
        <>
            <Title order={3}>{selectedElement.title}</Title>
            <Badge color="teal">{t(`topic.contentElementType.${selectedElement.type}`)}</Badge>
            <ContentElementDisplay contentElement={selectedElement} />
        </>
    )
}

export default ContentSidebarContent;