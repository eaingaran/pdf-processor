package xyz.fullstack.development;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@SpringBootApplication
@EnableBatchProcessing
@EnableScheduling
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private ApplicationContext applicationContext;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Scheduled(fixedDelay = 10000)
    public void runPdfProcessor() throws Exception {
        String path = "input/";
        String pattern = "*.pdf";
        System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");

        System.out.println("Scheduled method invoked :: " + System.nanoTime());
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<JobExecution>> jobExecutionList = new ArrayList<>();
        Job job = (Job) applicationContext.getBean("pdfProcessor");
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(this.getClass().getClassLoader());
        Resource[] resources = resolver.getResources("file:" + path + pattern);
        for (Resource resource : resources) {
            System.out.println(resource.getFilename());
            System.out.println("Adding file :" + path + resource.getFilename() + ": to the job...");
            jobExecutionList.add(executor.submit(() -> {
                JobParametersBuilder builder = new JobParametersBuilder();
                builder.addString("time", Long.toString(System.nanoTime()));
                builder.addString("jsonPath", "/Users/aingaran/development/pdf_processor/target/output/");
                builder.addString("inputFile", resource.getURI().toString());
                builder.addString("outputFile", resource.getURI().toString().replace(".pdf", "_output.pdf"));
                return jobLauncher.run(job, builder.toJobParameters());
            }));
        }
        for (Future<JobExecution> jobExecutionFuture : jobExecutionList) {
            JobExecution jobExecution = jobExecutionFuture.get();
            log.info("Job Execution :: " + jobExecution.toString());
            log.info("Job Status    :: " + jobExecution.getStatus());
        }
    }

}
