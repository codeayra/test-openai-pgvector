package com.springai.test_openai_pgvector;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class AskController {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    @Value("classpath:/prompts/spring-projects.st")
    private Resource sbPromptTemplate;

    public AskController(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
    }

    @GetMapping("/ai/ask")
    public String question(@RequestParam(defaultValue = "What is Spring Boot") String message) {
        PromptTemplate promptTemplate = new PromptTemplate(sbPromptTemplate);
        Map<String, Object> promptParameters = new HashMap<>();
        promptParameters.put("input", message);
        promptParameters.put("documents", String.join("\n", findSimilarDocuments(message)));
        Prompt prompt = promptTemplate.create(promptParameters);
        return chatClient.prompt(prompt).call().content();
    }

    private List<String> findSimilarDocuments(String message) {
        List<Document> similarDocuments = vectorStore.similaritySearch(SearchRequest.builder().query(message).topK(3).build());
        return similarDocuments.stream().map(Document::getText).toList();
    }

}
