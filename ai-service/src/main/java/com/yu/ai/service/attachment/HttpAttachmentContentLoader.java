package com.yu.ai.service.attachment;

import com.yu.api.dto.AiChatAttachmentDTO;
import com.yu.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.net.URI;

@Component
@RequiredArgsConstructor
public class HttpAttachmentContentLoader implements AttachmentContentLoader {

    private final RestClient.Builder restClientBuilder;

    @Override
    public DownloadedAttachment load(AiChatAttachmentDTO attachmentDTO) {
        try {
            URI uri = URI.create(attachmentDTO.getUrl());
            ResponseEntity<byte[]> responseEntity = restClientBuilder.build()
                    .get()
                    .uri(uri)
                    .retrieve()
                    .toEntity(byte[].class);
            byte[] bytes = responseEntity.getBody();
            if (bytes == null || bytes.length == 0) {
                throw new BadRequestException("附件内容为空: " + attachmentDTO.getUrl());
            }
            String fileName = StringUtils.hasText(attachmentDTO.getFileName())
                    ? attachmentDTO.getFileName().trim()
                    : (StringUtils.hasText(org.springframework.util.StringUtils.getFilename(uri.getPath()))
                    ? org.springframework.util.StringUtils.getFilename(uri.getPath())
                    : "attachment");
            String contentType = responseEntity.getHeaders().getContentType() != null
                    ? responseEntity.getHeaders().getContentType().toString()
                    : attachmentDTO.getContentType();
            return new DownloadedAttachment(attachmentDTO.getUrl(), fileName, contentType, bytes);
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("附件下载失败: " + attachmentDTO.getUrl(), e);
        }
    }
}