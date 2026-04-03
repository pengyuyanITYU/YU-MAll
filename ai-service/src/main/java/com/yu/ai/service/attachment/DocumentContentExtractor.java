package com.yu.ai.service.attachment;

import com.yu.common.exception.BadRequestException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class DocumentContentExtractor {

    static final int MAX_DOCUMENT_CHARS = 12000;

    public String extract(SupportedAttachmentType attachmentType, DownloadedAttachment attachment) {
        try {
            String content = switch (attachmentType) {
                case PDF -> extractPdf(attachment.bytes());
                case WORD -> extractWord(attachment);
                case EXCEL -> extractExcel(attachment.bytes());
                case IMAGE -> throw new BadRequestException("Image attachments do not require text extraction");
            };
            if (!StringUtils.hasText(content)) {
                throw new BadRequestException("Document content is empty: " + attachment.fileName());
            }
            return trimContent(content.trim());
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Attachment parsing failed: " + attachment.fileName(), e);
        }
    }

    String trimContent(String content) {
        if (!StringUtils.hasText(content) || content.length() <= MAX_DOCUMENT_CHARS) {
            return content;
        }
        return content.substring(0, MAX_DOCUMENT_CHARS);
    }

    private String extractPdf(byte[] bytes) throws Exception {
        try (PDDocument document = Loader.loadPDF(bytes)) {
            return new PDFTextStripper().getText(document);
        }
    }

    private String extractWord(DownloadedAttachment attachment) throws Exception {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(attachment.bytes())) {
            if (SupportedAttachmentType.isDocx(attachment)) {
                try (XWPFDocument document = new XWPFDocument(inputStream);
                     XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
                    return extractor.getText();
                }
            }
            try (WordExtractor extractor = new WordExtractor(inputStream)) {
                return extractor.getText();
            }
        }
    }

    private String extractExcel(byte[] bytes) throws Exception {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            DataFormatter formatter = new DataFormatter();
            StringBuilder builder = new StringBuilder();
            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                builder.append("[Sheet] ").append(sheet.getSheetName()).append('\n');
                for (Row row : sheet) {
                    List<String> cells = new ArrayList<>();
                    short lastCellNum = row.getLastCellNum();
                    if (lastCellNum < 0) {
                        continue;
                    }
                    for (int cellIndex = 0; cellIndex < lastCellNum; cellIndex++) {
                        String value = formatter.formatCellValue(row.getCell(cellIndex));
                        if (StringUtils.hasText(value)) {
                            cells.add(value.trim());
                        }
                    }
                    if (!cells.isEmpty()) {
                        builder.append(String.join(" | ", cells)).append('\n');
                    }
                }
                builder.append('\n');
            }
            return builder.toString();
        }
    }
}