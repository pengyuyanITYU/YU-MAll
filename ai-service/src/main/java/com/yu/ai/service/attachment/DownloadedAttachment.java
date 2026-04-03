package com.yu.ai.service.attachment;

public record DownloadedAttachment(String url, String fileName, String contentType, byte[] bytes) {
}