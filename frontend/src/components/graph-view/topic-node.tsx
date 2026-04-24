import { IconBook } from "@tabler/icons-react";
import type { Topic } from "../../schemas/topic";
import HexagonNode from "./hexagon-node";
import type { NodeProps } from "@xyflow/react";


interface TopicNodeProps extends NodeProps {
    topic: Topic;
}

const TopicNode = ({ topic, ...props }: TopicNodeProps) => (
    <HexagonNode label={topic.title} color="#e03131" Icon={IconBook} labelSize="md" iconSize={50} {...props} />
)

export default TopicNode;