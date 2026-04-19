import { z } from 'zod';
import { BaseEntitySchema } from './common.ts';

export const ContentElementTypeSchema = z.enum([
  'PDF',
  'VIDEO_FILE',
  'AUDIO_FILE',
  'YOUTUBE_LINK',
  'SPOTIFY_LINK',
  'RTF',
  'URI',
  'IMAGE',
]);

export type ContentElementType = z.infer<typeof ContentElementTypeSchema>

// ---- REQUESTS ----

const ContentElementRequestSchema = z.object({
  title: z.string(),
  type: ContentElementTypeSchema,
});

export const PdfElementRequestSchema = z
  .object({
    originalFileName: z.string(),
  })
  .extend(ContentElementRequestSchema.shape);

export type PdfElementRequest = z.infer<typeof PdfElementRequestSchema>;

export const VideoFileElementRequestSchema = z
  .object({
    originalFileName: z.string(),
  })
  .extend(ContentElementRequestSchema.shape);

export type VideoFileElementRequest = z.infer<typeof VideoFileElementRequestSchema>;

export const AudioFileElementRequestSchema = z
  .object({
    originalFileName: z.string(),
  })
  .extend(ContentElementRequestSchema.shape);

export type AudioFileElementRequest = z.infer<typeof AudioFileElementRequestSchema>;

export const SpotifyLinkElementRequestSchema = z
  .object({
    uri: z.url(),
  })
  .extend(ContentElementRequestSchema.shape);

export type SpotifyLinkRequest = z.infer<typeof SpotifyLinkElementRequestSchema>;

export const YouTubeLinkElementRequestSchema = z
  .object({
    uri: z.url(),
  })
  .extend(ContentElementRequestSchema.shape);

export type YouTubeLinkElementRequest = z.infer<typeof YouTubeLinkElementRequestSchema>;

export const RtfElementRequestSchema = z
  .object({
    rtfText: z.string(),
  })
  .extend(ContentElementRequestSchema.shape);

export type RtfElementRequest = z.infer<typeof RtfElementRequestSchema>;

export const UriElementRequestSchema = z
  .object({
    uri: z.url(),
  })
  .extend(ContentElementRequestSchema.shape);

export type UriElementRequest = z.infer<typeof UriElementRequestSchema>;

export const ImageElementRequestSchema = z
  .object({
    originalFileName: z.string(),
  })
  .extend(ContentElementRequestSchema.shape);

export type ImageElementRequest = z.infer<typeof ImageElementRequestSchema>;

export type AnyContentElementRequest =
  | PdfElementRequest
  | VideoFileElementRequest
  | AudioFileElementRequest
  | YouTubeLinkElementRequest
  | SpotifyLinkRequest
  | RtfElementRequest
  | UriElementRequest
  | ImageElementRequest;

// ---- RESPONSES ----

const ContentElementDtoSchema = z
  .object({
    title: z.string(),
    type: ContentElementTypeSchema,
  })
  .extend(BaseEntitySchema.shape);

export const PdfElementDtoSchema = z
  .object({
    presignedUrl: z.url(),
    originalFileName: z.string(),
  })
  .extend(ContentElementDtoSchema.shape);

export type PdfElementDto = z.infer<typeof PdfElementDtoSchema>;

export const VideoFileElementDtoSchema = z
  .object({
    presignedUrl: z.url(),
    originalFileName: z.string(),
  })
  .extend(ContentElementDtoSchema.shape);

export type VideoFileElementDto = z.infer<typeof VideoFileElementDtoSchema>;

export const AudioFileElementDtoSchema = z
  .object({
    presignedUrl: z.url(),
    originalFileName: z.string(),
  })
  .extend(ContentElementDtoSchema.shape);

export type AudioFileElementDto = z.infer<typeof AudioFileElementDtoSchema>;

export const YouTubeLinkElementDtoSchema = z
  .object({
    uri: z.url(),
  })
  .extend(ContentElementDtoSchema.shape);

export type YouTubeLinkElementDto = z.infer<typeof YouTubeLinkElementDtoSchema>;

export const SpotifyLinkElementDtoSchema = z
  .object({
    uri: z.url(),
  })
  .extend(ContentElementDtoSchema.shape);

export type SpotifyLinkElementDto = z.infer<typeof SpotifyLinkElementDtoSchema>;

export const RtfElementDtoSchema = z
  .object({
    rtfText: z.string(),
  })
  .extend(ContentElementDtoSchema.shape);

export type RtfElementDto = z.infer<typeof RtfElementDtoSchema>;

export const UriElementDtoSchema = z
  .object({
    uri: z.url(),
  })
  .extend(ContentElementDtoSchema.shape);

export type UriElementDto = z.infer<typeof UriElementDtoSchema>;

export const ImageElementDtoSchema = z
  .object({
    presignedUrl: z.url(),
    originalFileName: z.string(),
  })
  .extend(ContentElementDtoSchema.shape);

export type ImageElementDto = z.infer<typeof ImageElementDtoSchema>;

export const AnyContentElementDtoSchema = PdfElementDtoSchema.or(VideoFileElementDtoSchema).or(AudioFileElementDtoSchema).or(YouTubeLinkElementDtoSchema).or(SpotifyLinkElementDtoSchema).or(RtfElementDtoSchema).or(UriElementDtoSchema).or(ImageElementDtoSchema);

export type AnyContentElementDto = z.infer<typeof AnyContentElementDtoSchema>;