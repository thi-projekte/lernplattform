import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiClient, safeValidateApiResponseContent } from './common.ts';
import { TopicNoteDtoSchema, type TopicNoteDto } from '../schemas/note.ts';

const fetchTopicNote = async (topicId: string): Promise<TopicNoteDto> => {
  const result = await apiClient.get(`/topic-notes/${topicId}`, {
    validateStatus: (status) => status <= 204,
  });
  return safeValidateApiResponseContent(TopicNoteDtoSchema, result.data);
};

export const useQueryTopicNote = (topicId: string) =>
  useQuery({
    queryKey: ['topicNote', topicId],
    enabled: topicId.length > 0,
    queryFn: () => fetchTopicNote(topicId),
  });

const updateTopicNote = async (topicId: string, content: string): Promise<TopicNoteDto> => {
  const result = await apiClient.put(
    `/topic-notes/${topicId}`,
    { content },
    { validateStatus: (status) => status <= 204 }
  );
  return safeValidateApiResponseContent(TopicNoteDtoSchema, result.data);
};

export const useUpdateTopicNoteMutation = (topicId: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationKey: ['updateTopicNote', topicId],
    mutationFn: (content: string) => updateTopicNote(topicId, content),
    onSuccess: (data) => {
      queryClient.setQueryData(['topicNote', topicId], data);
    },
  });
};
