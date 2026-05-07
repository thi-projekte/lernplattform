import type { Node } from '@xyflow/react';
import type { GraphTopicDto } from '../../schemas/topic-graph.ts';

export type SkillTreeOrientation = 'vertical' | 'horizontal';
export type SkillTreeNodeRole = 'root' | 'connected' | 'disconnected';

export interface SkillTreeNodeData extends Record<string, unknown> {
  kind: 'skill-topic';
  title: string;
  categories: GraphTopicDto['categories'];
  creatorFullName?: string;
  role: SkillTreeNodeRole;
  payload: GraphTopicDto;
}

export type SkillTreeNode = Node<SkillTreeNodeData>;
