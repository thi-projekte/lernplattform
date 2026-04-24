import { IconBook } from '@tabler/icons-react';
import type { Topic } from '../../schemas/topic';
import HexagonNode from './hexagon-node';
import type { NodeProps, Node } from '@xyflow/react';

type TopicNodeProps = NodeProps<Node<Topic>>;

const TopicNode = ({ data, ...props }: TopicNodeProps) => (
  <HexagonNode
    label={data.title}
    color="#e03131"
    Icon={IconBook}
    labelSize="sm"
    labelFontWeight={700}
    size={80}
    {...props}
  />
);

export default TopicNode;
