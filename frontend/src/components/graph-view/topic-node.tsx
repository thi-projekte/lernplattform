import { IconBook } from '@tabler/icons-react';
import HexagonNode from './hexagon-node';
import type { NodeProps, Node } from '@xyflow/react';
import type { GraphTopicNodeData } from './topic-graph.types';

type TopicNodeProps = NodeProps<Node<GraphTopicNodeData>>;

const TopicNode = ({ data, ...props }: TopicNodeProps) => (
  <HexagonNode
    label={data.title}
    color={data.isRoot ? '#e03131' : '#f08c00'}
    Icon={IconBook}
    labelSize="sm"
    labelFontWeight={700}
    size={80}
    subLabel={data.creatorFullName}
    {...props}
  />
);

export default TopicNode;
