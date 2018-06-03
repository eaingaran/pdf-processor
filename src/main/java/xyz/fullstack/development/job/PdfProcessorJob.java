package xyz.fullstack.development.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import xyz.fullstack.development.batch.processor.PdfProcessor;
import xyz.fullstack.development.batch.reader.PdfReader;
import xyz.fullstack.development.batch.writer.PdfWriter;
import xyz.fullstack.development.domain.PdfObject;

@Component
public class PdfProcessorJob {

    private static final Logger log = LoggerFactory.getLogger(PdfProcessorJob.class);

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    @StepScope
    public PdfReader reader() {
        return new PdfReader();
    }

    @Bean
    @StepScope
    public PdfProcessor processor() {
        return new PdfProcessor();
    }

    @Bean
    @StepScope
    public PdfWriter writer() {
        return new PdfWriter();
    }


    @Bean
    public Job pdfProcessor() {
        return jobBuilderFactory.get("pdfProcessor")
                .start(step1())
                .build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .<PdfObject, PdfObject>chunk(10)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }
}
