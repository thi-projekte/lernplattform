import { z } from 'zod';

export const ProfilePictureDtoSchema = z.object({
  url: z.string(),
});

export type ProfilePictureDto = z.infer<typeof ProfilePictureDtoSchema>;
