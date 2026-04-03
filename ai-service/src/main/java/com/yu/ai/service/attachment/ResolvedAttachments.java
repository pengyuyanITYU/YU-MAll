package com.yu.ai.service.attachment;

import org.springframework.ai.content.Media;

import java.util.List;

public record ResolvedAttachments(List<Media> imageMediaList, List<DocumentAttachmentContent> documentContents) {

    public ResolvedAttachments {
        imageMediaList = imageMediaList == null ? List.of() : List.copyOf(imageMediaList);
        documentContents = documentContents == null ? List.of() : List.copyOf(documentContents);
    }
}