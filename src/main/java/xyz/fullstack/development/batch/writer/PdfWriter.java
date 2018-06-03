package xyz.fullstack.development.batch.writer;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.encoding.WinAnsiEncoding;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;
import xyz.fullstack.development.domain.PdfObject;

import javax.json.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.List;


public class PdfWriter implements ItemWriter<PdfObject>, StepExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(PdfWriter.class);

    private String file_name;
    private PrintWriter pw;
    private int current_page = 0;

    public void beforeStep(StepExecution var1) {
        file_name = var1.getJobExecution().getJobParameters().getString("outputFile");
        String jsonPath = var1.getJobExecution().getJobParameters().getString("jsonPath");
        boolean foldersCreated = new File(jsonPath).mkdirs();
        logger.info("Folders create? :: " + foldersCreated);
        try {
            pw = new PrintWriter(new File(jsonPath +
                    new File(URI.create(file_name).getPath()).getName()
                            .replace(".pdf", ".json")).toURI().getPath());
        } catch (IOException e) {
            logger.error("IOException while writing JSON", e);
        }
        logger.info("Writing to file :: " + file_name);
    }

    public ExitStatus afterStep(StepExecution var1) {
        pw.close();
        logger.info("Total Pages written :: " + current_page);
        return ExitStatus.COMPLETED;
    }

    public void write(List<? extends PdfObject> var1) throws Exception {
        File file = new File(URI.create(file_name).getPath());
        JsonWriter jsonWriter = Json.createWriter(pw);
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        boolean fileExists = file.exists();
        PDDocument document;
        if(fileExists) {
            document = PDDocument.load(file);
        } else  {
            document = new PDDocument();
        }
        for (PdfObject obj : var1) {
            jsonArrayBuilder.add(obj.getId());

            PDPage page = new PDPage();

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.beginText();
            contentStream.setFont(PDType1Font.COURIER, 12);
            contentStream.newLineAtOffset(25, 50);
            contentStream.showText(remove(obj.getId()));
            contentStream.endText();
            if(obj.getQrCodePath() != null) {
                PDImageXObject pdImage = PDImageXObject.createFromFile(obj.getQrCodePath(), document);
                contentStream.drawImage(pdImage, 70, 250);
            }
            contentStream.close();

            document.addPage(page);
            current_page += 1;
        }
        JsonArray jsonArray = jsonArrayBuilder.build();
        jsonWriter.writeArray(jsonArray);
        if(fileExists) {
            document.close();
        } else {
            document.save(URI.create(file_name).getPath());
        }
    }

    private String remove(String test) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < test.length(); i++) {
            if (WinAnsiEncoding.INSTANCE.contains(test.charAt(i))) {
                b.append(test.charAt(i));
            } else  {
                logger.warn("Page '" + current_page + "' contains invalid character '" + test.charAt(i) +
                        "' at the position '" + i + "'. \nThis will be ignored...");
            }
        }
        return b.toString();
    }


}
