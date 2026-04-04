package com.yu.ai.service.attachment;

import com.yu.common.exception.BadRequestException;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.Locale;
import java.util.Set;

public enum SupportedAttachmentType {
    IMAGE,
    PDF,
    WORD,
    EXCEL;

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("png", "jpg", "jpeg", "webp");
    private static final Set<String> WORD_EXTENSIONS = Set.of("doc", "docx");
    private static final Set<String> EXCEL_EXTENSIONS = Set.of("xls", "xlsx");
    private static final String OCTET_STREAM = "application/octet-stream";
    private static final String DOCX_MIME = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private static final String XLSX_MIME = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    public static SupportedAttachmentType detect(AttachmentContentLoader.DownloadedAttachment attachment) {
        String mimeType = normalizeMimeType(attachment.contentType());
        String extension = resolveExtension(attachment.fileName(), attachment.url());
        if (StringUtils.hasText(mimeType) && !OCTET_STREAM.equals(mimeType)) {
            if (mimeType.startsWith("image/")) {
                return IMAGE;
            }
            if ("application/pdf".equals(mimeType)) {
                return PDF;
            }
            if ("application/msword".equals(mimeType) || DOCX_MIME.equals(mimeType)) {
                return WORD;
            }
            if ("application/vnd.ms-excel".equals(mimeType) || XLSX_MIME.equals(mimeType)) {
                return EXCEL;
            }
        }
        if (IMAGE_EXTENSIONS.contains(extension)) {
            return IMAGE;
        }
        if ("pdf".equals(extension)) {
            return PDF;
        }
        if (WORD_EXTENSIONS.contains(extension)) {
            return WORD;
        }
        if (EXCEL_EXTENSIONS.contains(extension)) {
            return EXCEL;
        }
        throw new BadRequestException("Unsupported attachment type: " + resolveDisplayName(attachment.fileName(), attachment.url()));
    }

    public static MimeType resolveImageMimeType(AttachmentContentLoader.DownloadedAttachment attachment) {
        String mimeType = normalizeMimeType(attachment.contentType());
        if (StringUtils.hasText(mimeType) && mimeType.startsWith("image/")) {
            return MimeType.valueOf(mimeType);
        }
        String extension = resolveExtension(attachment.fileName(), attachment.url());
        return switch (extension) {
            case "jpg", "jpeg" -> MimeTypeUtils.IMAGE_JPEG;
            case "webp" -> MimeType.valueOf("image/webp");
            default -> MimeTypeUtils.IMAGE_PNG;
        };
    }

    public static boolean isDocx(AttachmentContentLoader.DownloadedAttachment attachment) {
        String mimeType = normalizeMimeType(attachment.contentType());
        return DOCX_MIME.equals(mimeType) || "docx".equals(resolveExtension(attachment.fileName(), attachment.url()));
    }

    static String resolveExtension(String fileName, String url) {
        String extension = normalizeExtension(StringUtils.getFilenameExtension(fileName));
        if (StringUtils.hasText(extension)) {
            return extension;
        }
        try {
            URI uri = URI.create(url);
            return normalizeExtension(StringUtils.getFilenameExtension(uri.getPath()));
        } catch (Exception e) {
            return "";
        }
    }

    private static String normalizeMimeType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return "";
        }
        int separatorIndex = contentType.indexOf(';');
        String value = separatorIndex >= 0 ? contentType.substring(0, separatorIndex) : contentType;
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeExtension(String extension) {
        return extension == null ? "" : extension.trim().toLowerCase(Locale.ROOT);
    }

    private static String resolveDisplayName(String fileName, String url) {
        if (StringUtils.hasText(fileName)) {
            return fileName;
        }
        return StringUtils.hasText(url) ? url : "unknown";
    }
}