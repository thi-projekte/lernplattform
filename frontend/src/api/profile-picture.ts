import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiClient, safeValidateApiResponseContent } from './common.ts';
import { type ProfilePictureDto, ProfilePictureDtoSchema } from '../schemas/profile-picture.ts';

const uploadProfilePicture = async (file: File): Promise<ProfilePictureDto> => {
  const formData = new FormData();
  formData.append('file', file);
  const result = await apiClient.post('/auth/profile-picture', formData, {
    validateStatus: (status) => status === 201,
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return safeValidateApiResponseContent(ProfilePictureDtoSchema, result.data);
};

const deleteProfilePicture = async () =>
  await apiClient.delete('/auth/profile-picture', {
    validateStatus: (status) => status === 200,
  });

const fetchProfilePicture = async (username: string): Promise<ProfilePictureDto | null> => {
  const result = await apiClient.get(`/auth/profile-picture/${username}`, {
    validateStatus: (status) => status === 200 || status === 404,
  });
  if (result.status === 404) {
    return null;
  }
  return safeValidateApiResponseContent(ProfilePictureDtoSchema, result.data);
};

export const useQueryProfilePicture = (username: string | undefined) => {
  return useQuery({
    queryKey: ['profilePicture', username],
    enabled: !!username,
    queryFn: () => fetchProfilePicture(username!),
  });
};

export const useUploadProfilePictureMutation = (username: string | undefined) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationKey: ['uploadProfilePicture'],
    mutationFn: uploadProfilePicture,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['profilePicture', username] });
    },
  });
};

export const useDeleteProfilePictureMutation = (username: string | undefined) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationKey: ['deleteProfilePicture'],
    mutationFn: deleteProfilePicture,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['profilePicture', username] });
    },
  });
};
