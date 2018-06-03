package xyz.fullstack.development.batch.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemProcessor;
import xyz.fullstack.development.batch.reader.PdfReader;
import xyz.fullstack.development.domain.PdfObject;


public class PdfProcessor implements ItemProcessor<PdfObject, PdfObject>, StepExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(PdfReader.class);

    public void beforeStep(StepExecution var1) {
        logger.info("Inside processor");
    }

    public ExitStatus afterStep(StepExecution var1) {
        logger.info("All processing completed...");
        return ExitStatus.COMPLETED;
    }

    public PdfObject process(PdfObject var1) {
        /*
        Send the id (var1.getId()) to the system that provides QR code,
        Save the QR Code in a temporary location as an image.
        attach the image path to the object as shown in sample below...
         */
        var1.setQrCodePath("input/sample.jpeg");
        return var1;
    }

}
