package com.yu.ai.service.attachment;

import com.yu.api.dto.AiChatAttachmentDTO;
import com.yu.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AiAttachmentResolver {

    private static final int MAX_ATTACHMENT_COUNT = 5;

    private final AttachmentContentLoader attachmentContentLoader;
    private final DocumentContentExtractor documentContentExtractor;

    public ResolvedAttachments resolve(List<AiChatAttachmentDTO> attachments) {
        if (CollectionUtils.isEmpty(attachments)) {
            return new ResolvedAttachments(List.of(), List.of());
        }
        if (attachments.size() > MAX_ATTACHMENT_COUNT) {
            throw new BadRequestException("At most 5 attachments are supported");
        }
        List<Media> imageMediaList = new ArrayList<>();
        List<DocumentAttachmentContent> documentContents = new ArrayList<>();
        for (AiChatAttachmentDTO attachment : attachments) {
            if (attachment == null || !StringUtils.hasText(attachment.getUrl())) {
                throw new BadRequestException("Attachment url cannot be blank");
            }
            AttachmentContentLoader.DownloadedAttachment downloadedAttachment = attachmentContentLoader.load(attachment);
            SupportedAttachmentType attachmentType = SupportedAttachmentType.detect(downloadedAttachment);
            if (attachmentType == SupportedAttachmentType.IMAGE) {
                imageMediaList.add(new Media(
                        SupportedAttachmentType.resolveImageMimeType(downloadedAttachment),
                        new NamedByteArrayResource(downloadedAttachment.bytes(), downloadedAttachment.fileName())
                ));
                continue;
            }
            documentContents.add(new DocumentAttachmentContent(
                    downloadedAttachment.fileName(),
                    downloadedAttachment.contentType(),
                    documentContentExtractor.extract(attachmentType, downloadedAttachment)
            ));
        }
        return new ResolvedAttachments(imageMediaList, documentContents);
    }

    public record DocumentAttachmentContent(String fileName, String contentType, String content) {
    }

    public record ResolvedAttachments(List<Media> imageMediaList, List<DocumentAttachmentContent> documentContents) {

        public ResolvedAttachments {
            imageMediaList = imageMediaList == null ? List.of() : List.copyOf(imageMediaList);
            documentContents = documentContents == null ? List.of() : List.copyOf(documentContents);
        }
    }

    private static final class NamedByteArrayResource extends ByteArrayResource {

        private final String fileName;

        private NamedByteArrayResource(byte[] byteArray, String fileName) {
            super(byteArray);
            this.fileName = fileName;
        }

        @Override
        public String getFilename() {
            return fileName;
        }
    }
}