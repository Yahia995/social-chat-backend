# WebSocket Testing Guide

## Overview

This document describes how to run and verify the WebSocket implementation tests for the Social Chat Backend.

## Test Structure

Tests have been reorganized into the proper Maven package structure:

\`\`\`
src/test/java/com/socialchat/
├── config/
│   └── WebSocketConfigTest.java          # Configuration validation tests
├── controller/
│   ├── WebSocketControllerIntegrationTest.java  # Integration tests
│   └── WebSocketErrorHandlingTest.java          # Error handling tests
└── util/
    ├── WebSocketTestDataSeeder.java       # Test data generator
    └── WebSocketTestDataSeederTest.java   # Seeder validation tests
\`\`\`

## Prerequisites

1. **MySQL Test Database**: Ensure you have a MySQL instance with a `social_chat_test_db` database
   \`\`\`sql
   CREATE DATABASE IF NOT EXISTS social_chat_test_db;
   \`\`\`

2. **Test Configuration**: The application uses `application-test.yml` with:
   - Test database: `social_chat_test_db`
   - Automatic schema creation via Hibernate (`ddl-auto: create-drop`)
   - Test data seeding enabled (`seed-test-data: true`)

3. **Java 21 and Maven 3.6+**

## Running Tests

### Run All WebSocket Tests
\`\`\`bash
mvn test -Dtest=WebSocket*
\`\`\`

### Run Specific Test Class
\`\`\`bash
mvn test -Dtest=WebSocketControllerIntegrationTest
mvn test -Dtest=WebSocketConfigTest
mvn test -Dtest=WebSocketErrorHandlingTest
\`\`\`

### Run With Verbose Output
\`\`\`bash
mvn test -Dtest=WebSocket* -X
\`\`\`

### Run With Specific Profile
\`\`\`bash
mvn test -Dspring.profiles.active=test
\`\`\`

## Test Coverage

### WebSocketConfigTest
- Validates WebSocket configuration bean exists
- Verifies endpoint registration
- Checks CORS and STOMP configuration

### WebSocketControllerIntegrationTest
- WebSocket connection with valid JWT token
- Connection rejection with invalid/missing token
- Message broadcasting to conversation topic
- Typing indicator broadcasting
- User presence online/offline status
- Invalid message content handling

### WebSocketErrorHandlingTest
- Revoked token rejection
- Malformed token handling
- Connection lifecycle management

### WebSocketTestDataSeederTest
- User creation (testuser1, testuser2, testuser3, testuser4)
- Conversation setup between users
- Message generation and sequence
- Post creation with test content
- Friendship/connection seeding
- User presence records
- Post interactions (likes, comments)

## Test Data

The `WebSocketTestDataSeeder` automatically populates:

- **4 Test Users**: testuser1-testuser4 with emails and profiles
- **Conversations**: One-on-one chats between users
- **Messages**: 5+ sample messages in conversations
- **Posts**: User-generated content with interactions
- **Presence**: Online/offline status for each user
- **Interactions**: Likes and comments on posts

### Seeding Behavior

Seeding is controlled by the `app.seed-test-data` property in `application-test.yml`:
- Set to `true` to auto-seed on test startup
- Set to `false` to manually seed or use existing data

### Manual Seeding

\`\`\`java
@Autowired
private WebSocketTestDataSeeder seeder;

@Test
void myTest() {
    seeder.seedAll(); // Seeds all test data
    // or individual methods:
    seeder.seedTestUsers();
    seeder.seedTestConversations();
    seeder.seedTestMessages();
    // etc...
}
\`\`\`

## Database Reset Between Tests

Each test class uses `@BeforeEach` to clear test data:
\`\`\`java
@BeforeEach
void setUp() {
    messageRepository.deleteAll();
    conversationRepository.deleteAll();
    // ... etc
}
\`\`\`

This ensures test isolation and clean state for each test.

## Expected Test Results

All tests should pass with output like:
\`\`\`
[INFO] Tests run: 18, Failures: 0, Errors: 0, Skipped: 0
\`\`\`

## Troubleshooting

### Tests Not Found
Ensure package names are correct and match the new structure:
- Config tests: `com.socialchat.config`
- Controller tests: `com.socialchat.controller`
- Utility tests: `com.socialchat.util`

### Database Connection Errors
1. Verify MySQL is running
2. Check `application-test.yml` credentials
3. Ensure `social_chat_test_db` exists
4. Connection string should use `allowPublicKeyRetrieval=true`

### JWT Token Errors
The test configuration includes a default test JWT secret. If modified in production, update the same secret in `application-test.yml` for tests to pass.

### WebSocket Connection Timeouts
- Increase timeout in tests if running on slow machines
- Current timeout: 10 seconds for connections
- Adjust in test methods: `.get(10, TimeUnit.SECONDS)`

## CI/CD Integration

For continuous integration pipelines:

\`\`\`yaml
# Example GitHub Actions
- name: Run WebSocket Tests
  run: mvn test -Dtest=WebSocket* -P test
\`\`\`

## Next Steps

- Monitor test coverage with `mvn jacoco:report`
- Add load tests for high-concurrency scenarios
- Implement performance benchmarks
- Add tests for message persistence and recovery
