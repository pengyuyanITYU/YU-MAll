package com.yu.ai.service.attachment;

import com.yu.api.dto.AiChatAttachmentDTO;
import com.yu.common.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.content.Media;
import org.springframework.util.MimeTypeUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiAttachmentResolverTest {

    private final AttachmentContentLoader attachmentContentLoader = mock(AttachmentContentLoader.class);
    private final DocumentContentExtractor documentContentExtractor = mock(DocumentContentExtractor.class);
    private AiAttachmentResolver aiAttachmentResolver;

    @BeforeEach
    void setUp() {
        aiAttachmentResolver = new AiAttachmentResolver(attachmentContentLoader, documentContentExtractor);
    }

    @Test
    void resolve_shouldConvertImageAttachmentToMedia() {
        AiChatAttachmentDTO attachmentDTO = buildAttachment("https://cdn.test/demo.png", "demo.png", "image/png");
        when(attachmentContentLoader.load(attachmentDTO)).thenReturn(new AttachmentContentLoader.DownloadedAttachment(
                "https://cdn.test/demo.png",
                "demo.png",
                "image/png",
                new byte[]{1, 2, 3}
        ));

        AiAttachmentResolver.ResolvedAttachments resolvedAttachments = aiAttachmentResolver.resolve(List.of(attachmentDTO));

        assertEquals(1, resolvedAttachments.imageMediaList().size());
        Media media = resolvedAttachments.imageMediaList().get(0);
        assertEquals(MimeTypeUtils.IMAGE_PNG, media.getMimeType());
        assertEquals(0, resolvedAttachments.documentContents().size());
    }

    @Test
    void resolve_shouldExtractDocumentText() {
        AiChatAttachmentDTO attachmentDTO = buildAttachment("https://cdn.test/report.pdf", "report.pdf", "application/pdf");
        AttachmentContentLoader.DownloadedAttachment downloadedAttachment = new AttachmentContentLoader.DownloadedAttachment(
                "https://cdn.test/report.pdf",
                "report.pdf",
                "application/pdf",
                new byte[]{9, 8, 7}
        );
        when(attachmentContentLoader.load(attachmentDTO)).thenReturn(downloadedAttachment);
        when(documentContentExtractor.extract(SupportedAttachmentType.PDF, downloadedAttachment)).thenReturn("document text");

        AiAttachmentResolver.ResolvedAttachments resolvedAttachments = aiAttachmentResolver.resolve(List.of(attachmentDTO));

        assertEquals(0, resolvedAttachments.imageMediaList().size());
        assertEquals(1, resolvedAttachments.documentContents().size());
        assertEquals("document text", resolvedAttachments.documentContents().get(0).content());
        verify(documentContentExtractor).extract(SupportedAttachmentType.PDF, downloadedAttachment);
    }

    @Test
    void resolve_shouldRejectUnsupportedAttachmentType() {
        AiChatAttachmentDTO attachmentDTO = buildAttachment("https://cdn.test/readme.txt", "readme.txt", "text/plain");
        when(attachmentContentLoader.load(attachmentDTO)).thenReturn(new AttachmentContentLoader.DownloadedAttachment(
                "https://cdn.test/readme.txt",
                "readme.txt",
                "text/plain",
                "hello".getBytes()
        ));

        assertThrows(BadRequestException.class, () -> aiAttachmentResolver.resolve(List.of(attachmentDTO)));
    }

    @Test
    void resolve_shouldRejectBlankAttachmentUrl() {
        AiChatAttachmentDTO attachmentDTO = buildAttachment(" ", "demo.png", "image/png");

        assertThrows(BadRequestException.class, () -> aiAttachmentResolver.resolve(List.of(attachmentDTO)));
    }

    private AiChatAttachmentDTO buildAttachment(String url, String fileName, String contentType) {
        AiChatAttachmentDTO attachmentDTO = new AiChatAttachmentDTO();
        attachmentDTO.setUrl(url);
        attachmentDTO.setFileName(fileName);
        attachmentDTO.setContentType(contentType);
        return attachmentDTO;
    }
}