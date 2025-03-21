# audioservice
### Architecture Overview
The system follows a monolithic architecture to handle the uploading, conversion, and downloading audio files.
Monolithic architecture was chosen for this project for some considerations:
- the expected traffic for this project is assumed to be at a low to medium level, which the monolithic architecture should be able to handle without any significant issues,
- to avoid over-engineering, which often results from adopting a microservices architecture too early,
- to simplify development, as the MVP has a straightforward scope that does not require the complexity of a microservices setup.

While a monolithic design is used initially, the architecture is designed in a modular fashion, making it easier to extract specific components into microservices if needed as the project scales.

### Architecture Layers
#### API Layer:
- Spring Boot REST Controller.
- Handles HTTP requests, routes to service layer.

#### Service Layer:
- Manages business logic.
- Handles file conversion and storage.
- Communicates with the repository layer for data persistence.
- Handles file storage in the local filesystem

#### Repository Layer:
- Uses Spring Data JPA to interact with MySQL.
- Manages storage and retrieval of file metadata.

#### Conversion Layer:
- Uses FFmpeg (via FFmpegWrapper) for audio format conversion.
- Converts audio files to different formats.

### API Endpoints

#### 1. Upload Audio File

   - Endpoint: POST /user/{userId}/phrase/{phraseId}
   - Description: Uploads a new audio file for a user id and a phrase id.
   - Request:
     - userId – ID of the user.
     - phraseId – ID of the phrase.
     - file – Audio file.
   - Response: File metadata (file ID, name, path).

#### 2. Download Audio File

   - Endpoint: GET "/v1/audio/user/{userId}/phrase/{phraseId}/{audioFormat}
   - Description: Downloads the latest audio file for the given user id, phrase id and valid audio format.
   - Request:
     - userId – ID of the user.
     - phraseId – ID of the phrase.
     - format – Desired audio format.
   - Response: Audio file in the specified format.

### Download Audio File Logic
The download process includes a complex decision-making flow to handle format conversion, file retrieval, and idempotency:

#### 1. File Selection:

- The system attempts to retrieve the latest file using a combination of `userId`, `phraseId`, and `groupId`:
  - If a `groupId` is provided, the system will prioritize the latest file within that group.
  - If a `groupId` is not provided, the system will attempt to find the latest file across all groups for the given user and phrase.
- The file selection is based on the `createdAt` timestamp, the most recently created file that matches the criteria is selected.

#### 2. Format Matching:

- If the selected file is already in the requested format, it is returned directly.
- If the file is in a different format:
  - The system attempts to convert it to the requested format using `FFmpeg`.
  - After conversion, the new file is stored for future requests.

#### 3. Missing File Handling:

- If no file matches the selection criteria (user ID, phrase ID, group ID, or format), a `ResourceNotFoundException` is thrown.

#### 4. Idempotency:

- The conversion and download flow ensures that the same file conversion does not happen multiple times.
- If a file in the requested format already exists, it is served directly to avoid redundant processing.

### Assumptions and Known Limitations

#### Assumptions:

- The system is assumed to handle low to medium traffic levels.
- FFmpeg is expected to handle audio format conversion reliably for common formats.

#### Limitations:
- The system currently stores files on the local filesystem, this may create scalability issues under high traffic.
- No caching, repeated requests for the same file format may lead to unnecessary processing.

### Next Steps and Future Improvements

- Idempotent File Uploads: Implement logic to prevent duplicate file uploads and storage, can use file checksum,
- Enhanced Logging: Add trace IDs and measure processing times for better monitoring,
- Caching: Introduce a caching layer to reduce load on the file system and improve response times,
- Authorization and Authentication: Implement validation to ensure that only authorized users can upload, download, and access audio files, preventing unauthorized access and protecting user data,
- Rate limit: Add rate limiting to prevent system overload, abuse, and ensure fair resource allocation among users,
- Scalability: Transition to a distributed storage solution; adopting microservices, separate services for upload download, and conversion.



Run service:
make up
make dev
make test

Alternatives:
docker compose up --build
docker compose -f docker-compose.dev.yml up --build
docker compose -f docker-compose.test.yml up --build
