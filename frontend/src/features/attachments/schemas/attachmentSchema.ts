import { z } from 'zod';

export const attachmentSchema = z.object({
  fileName: z.string().min(1).max(255),
  fileUrl: z.string().min(1),
  mimeType: z.string().max(150).optional(),
});
export type AttachmentFormData = z.infer<typeof attachmentSchema>;
