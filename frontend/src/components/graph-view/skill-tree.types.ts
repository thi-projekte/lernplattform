import type { Node } from '@xyflow/react';
import type { GraphTopicDto } from '../../schemas/topic-graph.ts';

export type SkillTreeOrientation = 'vertical' | 'horizontal';

export interface SkillTreeNodeData extends Record<string, unknown> {
  kind: 'skill-topic';
  title: string;
  categories: GraphTopicDto['categories'];
  creatorFullName?: string;
  payload: GraphTopicDto;
}

export type SkillTreeNode = Node<SkillTreeNodeData>;
