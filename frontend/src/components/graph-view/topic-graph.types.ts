import type { Node } from '@xyflow/react';
import type { AnyContentElementDto } from '../../schemas/content-element';
import type { GraphTopicDto } from '../../schemas/topic-graph.ts';
import type { Category, ListTopicDto, Topic } from '../../schemas/topic';

export interface TopicAssociationsGraphInput extends Record<string, unknown> {
  id?: string;
  title?: string;
  creatorFullName?: string;
  categories?: Category[];
  relatedTopics?: ListTopicDto[];
  isolatedTopics?: ListTopicDto[];
}

export interface GraphTopicNodeData extends Record<string, unknown> {
  kind: 'topic';
  graphNodeId?: string;
  title: string;
  creatorId?: string; // <-- Diese Zeile hinzufügen
  creatorFullName?: string;
  isRoot?: boolean;
  isIsolated?: boolean;
  isOwned?: boolean;
  payload:
    | TopicAssociationsGraphInput
    | Omit<Topic, 'relatedTopics'>
    | Topic
    | ListTopicDto
    | GraphTopicDto;
}

export interface GraphContentNodeData extends Record<string, unknown> {
  kind: 'content';
  title: string;
  payload: AnyContentElementDto;
}

export type TopicGraphNodeData = GraphTopicNodeData | GraphContentNodeData;

export type TopicGraphNode = Node<TopicGraphNodeData>;

export type TopicGraphNodePositions = Record<string, { x: number; y: number }>;
