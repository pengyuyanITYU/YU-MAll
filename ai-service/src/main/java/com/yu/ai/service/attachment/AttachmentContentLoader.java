package com.yu.ai.service.attachment;

import com.yu.api.dto.AiChatAttachmentDTO;

public interface AttachmentContentLoader {

    DownloadedAttachment load(AiChatAttachmentDTO attachmentDTO);

    record DownloadedAttachment(String url, String fileName, String contentType, byte[] bytes) {
    }
}