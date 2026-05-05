import { IconBook } from '@tabler/icons-react';
import HexagonNode from './hexagon-node';
import type { NodeProps, Node } from '@xyflow/react';
import type { GraphTopicNodeData } from './topic-graph.types';

type TopicNodeProps = NodeProps<Node<GraphTopicNodeData>>;

const TopicNode = ({ data, ...props }: TopicNodeProps) => {
  const color = data.isRoot ? '#e03131' : data.isIsolated ? '#5c7cfa' : '#f08c00';

  return (
    <HexagonNode
      label={data.title}
      color={color}
      Icon={IconBook}
      labelSize="sm"
      labelFontWeight={700}
      size={80}
      subLabel={data.creatorFullName}
      {...props}
    />
  );
};

export default TopicNode;
