package com.yu.ai.service.attachment;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentContentExtractorTest {

    private final DocumentContentExtractor documentContentExtractor = new DocumentContentExtractor();

    @Test
    void extract_shouldReadPdfContent() throws Exception {
        byte[] pdfBytes = createPdf("monthly report");

        String content = documentContentExtractor.extract(SupportedAttachmentType.PDF,
                new DownloadedAttachment("https://cdn.test/report.pdf", "report.pdf", "application/pdf", pdfBytes));

        assertTrue(content.contains("monthly report"));
    }

    @Test
    void extract_shouldReadDocxContent() throws Exception {
        byte[] docxBytes = createDocx("word content");

        String content = documentContentExtractor.extract(SupportedAttachmentType.WORD,
                new DownloadedAttachment("https://cdn.test/report.docx", "report.docx",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document", docxBytes));

        assertTrue(content.contains("word content"));
    }

    @Test
    void extract_shouldReadExcelContent() throws Exception {
        byte[] excelBytes = createXlsx();

        String content = documentContentExtractor.extract(SupportedAttachmentType.EXCEL,
                new DownloadedAttachment("https://cdn.test/report.xlsx", "report.xlsx",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes));

        assertTrue(content.contains("Sheet1"));
        assertTrue(content.contains("sku"));
        assertTrue(content.contains("100"));
    }

    @Test
    void extract_shouldTrimExcessivelyLongDocument() {
        String longText = "a".repeat(DocumentContentExtractor.MAX_DOCUMENT_CHARS + 20);

        String content = documentContentExtractor.trimContent(longText);

        assertEquals(DocumentContentExtractor.MAX_DOCUMENT_CHARS, content.length());
    }

    private byte[] createPdf(String text) throws Exception {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText(text);
                contentStream.endText();
            }
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    private byte[] createDocx(String text) throws Exception {
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XWPFParagraph paragraph = document.createParagraph();
            paragraph.createRun().setText(text);
            document.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private byte[] createXlsx() throws Exception {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Sheet1");
            var header = sheet.createRow(0);
            header.createCell(0).setCellValue("sku");
            header.createCell(1).setCellValue("sales");
            var data = sheet.createRow(1);
            data.createCell(0).setCellValue("SKU-1");
            data.createCell(1).setCellValue(100);
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}