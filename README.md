# Spring AI with PostgreSQL Vector Store

A Spring Boot application that demonstrates how to use Spring AI with PostgreSQL's pgvector extension to store and query PDF documents using vector embeddings.

## Features

- **PDF Document Processing**: Automatically loads and processes PDF documents (Spring Boot and Spring Web reference guides)
- **Vector Embeddings**: Uses OpenAI's embedding model to create vector representations of document content
- **PostgreSQL Vector Store**: Stores embeddings in PostgreSQL using the pgvector extension
- **Semantic Search**: Provides REST API endpoints to query documents using natural language
- **Intelligent Chunking**: Splits documents into optimal chunks for better retrieval

## Technology Stack

- **Java 17**
- **Spring Boot 3.5.5**
- **Spring AI 1.0.1**
- **PostgreSQL with pgvector extension**
- **OpenAI GPT-5 Nano**
- **Gradle**

## Prerequisites

- Java 17 or higher
- Docker and Docker Compose
- OpenAI API key

## Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd test-openai-pgvector
```

### 2. Set Environment Variables

Set your OpenAI API key:

```bash
export OPENAI_API_KEY=your_openai_api_key_here
```

### 3. Start PostgreSQL with pgvector

```bash
docker-compose up -d
```

This will start a PostgreSQL database with the pgvector extension on port 5432.

### 4. Run the Application

```bash
./gradlew bootRun
```

The application will:
- Automatically create the required database schema
- Load and process PDF documents from `src/main/resources/docs/`
- Generate embeddings and store them in the vector database

### 5. Query the Documents

Once the application is running, you can query the documents using the REST API:

```bash
# Ask a question about Spring Boot
curl "http://localhost:8080/ai/ask?message=What%20is%20Spring%20Boot?"

# Ask about Spring Web
curl "http://localhost:8080/ai/ask?message=How%20do%20I%20create%20a%20REST%20controller?"
```

## API Endpoints

### GET /ai/ask

Query the document store with natural language questions.

**Parameters:**
- `message` (optional): The question to ask. Defaults to "What is Spring Boot"

**Example:**
```bash
curl "http://localhost:8080/ai/ask?message=How%20do%20I%20configure%20Spring%20Boot?"
```

## Configuration

The application configuration can be found in `src/main/resources/application.properties`:

- **OpenAI Configuration**: API key and model settings
- **Database Configuration**: PostgreSQL connection details
- **Vector Store Configuration**: pgvector index and distance settings

### Key Configuration Options

```properties
# OpenAI settings
spring.ai.openai.api-key=${OPENAI_API_KEY:openai_api_key}
spring.ai.openai.chat.options.model=gpt-5-nano
spring.ai.openai.chat.options.temperature=1.0

# PostgreSQL connection
spring.datasource.url=jdbc:postgresql://localhost:5432/samplevectordb
spring.datasource.username=postgres
spring.datasource.password=postgres

# Vector store configuration
spring.vectorstore.pgvector.index-type=hnsw
spring.vectorstore.pgvector.distance-type=cosine_distance
spring.vectorstore.pgvector.dimensions=1536
```

## Project Structure

```
src/
├── main/
│   ├── java/com/springai/test_openai_pgvector/
│   │   ├── TestOpenaiPgvectorApplication.java    # Main Spring Boot application
│   │   ├── AskController.java                    # REST controller for queries
│   │   └── PdfLoader.java                        # PDF processing and loading
│   └── resources/
│       ├── application.properties                # Application configuration
│       ├── docker-compose.yml                   # PostgreSQL setup
│       ├── schema.sql                           # Database schema
│       ├── docs/                                # PDF documents to process
│       └── prompts/                             # AI prompt templates
```

## How It Works

1. **Document Loading**: The `PdfLoader` component automatically processes PDF documents on application startup
2. **Text Extraction**: PDFs are read page by page and text is extracted
3. **Chunking**: Documents are split into smaller chunks using a token-based text splitter
4. **Embedding Generation**: Each chunk is converted to a vector embedding using OpenAI's embedding model
5. **Vector Storage**: Embeddings are stored in PostgreSQL with metadata
6. **Query Processing**: When a question is asked, the system:
   - Converts the question to an embedding
   - Performs similarity search to find relevant document chunks
   - Uses the retrieved context to generate an answer via OpenAI

## Database Schema

The application uses a single table to store document embeddings:

```sql
CREATE TABLE vector_store (
    id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    content text,
    metadata json,
    embedding vector(1536)
);
```

## Development

### Building the Project

```bash
./gradlew build
```

### Running Tests

```bash
./gradlew test
```

### Adding New Documents

To add new PDF documents:

1. Place PDF files in `src/main/resources/docs/`
2. Update the `PdfLoader` class to reference the new resources
3. Restart the application

## Troubleshooting

### Common Issues

1. **OpenAI API Key**: Ensure your API key is set correctly
2. **Database Connection**: Make sure PostgreSQL is running and accessible
3. **Memory Issues**: Large PDFs may require increased JVM heap size

### Logs

Enable debug logging for vector store operations:

```properties
logging.level.org.springframework.ai.vectorstore=DEBUG
```

## License

This project is for demonstration purposes. Please ensure you comply with OpenAI's usage policies and any applicable licenses for the PDF documents used.
