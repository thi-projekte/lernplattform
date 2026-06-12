import { useQuery } from '@tanstack/react-query';
import { apiClient, safeValidateApiResponseContent } from './common.ts';
import { createPaginatedSchema } from '../schemas/common.ts';
import { ChatMessageDtoSchema } from '../schemas/chat.ts';
import { z } from 'zod';

const PaginatedChatMessagesSchema = createPaginatedSchema(ChatMessageDtoSchema);
type PaginatedChatMessages = z.infer<typeof PaginatedChatMessagesSchema>;

const fetchChatMessages = async (
  topicId: string,
  page: number,
  pageSize: number
): Promise<PaginatedChatMessages> => {
  const result = await apiClient.get(`/chats/${topicId}?page=${page}&pageSize=${pageSize}`, {
    validateStatus: (status) => status <= 204,
  });
  return safeValidateApiResponseContent(PaginatedChatMessagesSchema, result.data);
};

export const useQueryChatHistory = (topicId: string, pageSize = 50) =>
  useQuery({
    queryKey: ['chatMessages', topicId, pageSize],
    enabled: topicId.length > 0,
    queryFn: () => fetchChatMessages(topicId, 0, pageSize),
    // Load history once; live updates come over the WebSocket. Refetching on
    // window focus would re-run seedHistory and visibly reorder the list.
    staleTime: Infinity,
    refetchOnWindowFocus: false,
    refetchOnReconnect: false,
  });
