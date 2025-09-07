package com.springai.test_openai_pgvector;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PdfLoader {

    private final JdbcClient jdbcClient;
    private final VectorStore vectorStore;

    @Value("classpath:/docs/spring-web-reference.pdf")
    private Resource springWebResource;

    @Value("classpath:/docs/spring-boot-reference.pdf")
    private Resource springBootResource;


    public PdfLoader(JdbcClient jdbcClient, VectorStore vectorStore) {
        this.jdbcClient = jdbcClient;
        this.vectorStore = vectorStore;
    }

    @PostConstruct
    public void loadPdf() {
        log.info("Loading all PDF resources...");
        // Implement PDF loading and processing logic here
        // For example, extract text, generate embeddings, and store in vectorStore
        Integer count = jdbcClient.sql("select count(0) from vector_store").query(Integer.class).single();
        List<String> fileNames = jdbcClient
                .sql("SELECT DISTINCT metadata->>'file_name' AS file_name FROM vector_store")
                .query(String.class)
                .list();
        if (!fileNames.contains(springWebResource.getFilename())) {
            processAndSavePdf(springWebResource);
        }
        if (!fileNames.contains(springBootResource.getFilename())) {
            processAndSavePdf(springBootResource);
        } else {
            log.info("Vector store already has {} entries, skipping load.", count);
        }
    }

    private void processAndSavePdf(Resource resource) {
        log.info("Vector store is missing data for this resource, loading data for resource {}...", resource.getFilename());
        // Add logic to load and process the PDF, then populate the vector store
        var config = PdfDocumentReaderConfig.builder()
                .withPageExtractedTextFormatter(new ExtractedTextFormatter.Builder().withNumberOfBottomTextLinesToDelete(0)
                        .withNumberOfTopPagesToSkipBeforeDelete(0)
                        .build())
                .withPagesPerDocument(1)
                .build();
        var pdfReader = new PagePdfDocumentReader(resource, config);
        var textSplitter = new TokenTextSplitter();
        List<Document> documents = pdfReader.get();
        log.info("Extracted {} pages from resource {}", documents.size(), resource.getFilename());
        List<Document> processedDocs = documents.parallelStream()
                .flatMap(doc -> textSplitter.apply(List.of(doc)).stream())
                .collect(Collectors.toList());
        log.info("Split into {} chunks", processedDocs.size());
        vectorStore.accept(processedDocs);
        log.info("Resource {} Loaded...", resource.getFilename());
    }


}
