package xyz.fullstack.development.batch.reader;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemReader;
import xyz.fullstack.development.domain.PdfObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;


public class PdfReader implements ItemReader<PdfObject>, StepExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(PdfReader.class);

    private PDDocument document;
    private int totalPages = 0;
    private int currentPage = 0;


    public void beforeStep(StepExecution var1) {
        String file_name = var1.getJobExecution().getJobParameters().getString("inputFile");
        logger.info("Inside Reader :: " + file_name);
        try {
            document = PDDocument.load(new File(URI.create(file_name).getPath()));
            totalPages = document.getNumberOfPages();
        } catch (IOException e) {
            logger.error("IOException on File operation", e);
        }
    }

    public ExitStatus afterStep(StepExecution var1) {
        try {
            document.close();
        } catch (IOException e) {
            logger.error("Couldn't close the document", e);
        }
        logger.info("Total Pages found :: " + totalPages);
        logger.info("Total Pages Read  :: " + currentPage);
        return ExitStatus.COMPLETED;
    }

    public PdfObject read() throws Exception {
        if (currentPage < totalPages) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(currentPage);
            stripper.setEndPage(currentPage);
            String content = stripper.getText(document);
            currentPage += 1;
            return new PdfObject(content);
        } else {
            return null;
        }
    }
}
