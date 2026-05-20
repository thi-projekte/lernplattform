package de.thi.mynd.topic.rest;

import de.thi.mynd.topic.dto.importer.FullImportDto;
import de.thi.mynd.topic.service.ImportService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@QuarkusTest
class ImportResourceTest {

    @InjectMock
    ImportService importService;

    private FullImportDto validImportDto;

    @BeforeEach
    void setUp() {
        // Initialize a minimal DTO for testing
        validImportDto = new FullImportDto();
        // Set required fields here if your FullImportDto has @NotNull/validation constraints
        // e.g., validImportDto.setTopics(List.of(...));
    }

    @Test
    @TestSecurity(user = "test-builder", roles = {"builder"})
    void shouldImportTopicsSuccessfullyWhenUserHasBuilderRole() {
        // Act & Assert
        given()
                .contentType(ContentType.JSON)
                .body(validImportDto)
                .when()
                .post("/topics/import")
                .then()
                .statusCode(201);

        // Verify the mock service was called correctly with (dto, false)
        Mockito.verify(importService, Mockito.times(1))
                .importFull(any(FullImportDto.class), eq(false));
    }

    @Test
    @TestSecurity(user = "test-user", roles = {"user"}) // Authenticated, but wrong role
    void shouldReturnForbiddenWhenUserLacksBuilderRole() {
        given()
                .contentType(ContentType.JSON)
                .body(validImportDto)
                .when()
                .post("/topics/import")
                .then()
                .statusCode(403);

        // Verify service was never touched
        Mockito.verifyNoInteractions(importService);
    }

    @Test // No @TestSecurity annotation means no user session is present
    void shouldReturnUnauthorizedWhenNoUserAuthenticated() {
        given()
                .contentType(ContentType.JSON)
                .body(validImportDto)
                .when()
                .post("/topics/import")
                .then()
                .statusCode(401);

        Mockito.verifyNoInteractions(importService);
    }

    @Test
    @TestSecurity(user = "test-builder", roles = {"builder"})
    void shouldReturnBadRequestWhenDtoIsInvalid() {
        // If your FullImportDto has Jakarta Validation rules (like @NotNull, @Size),
        // passing an empty/invalid object should be caught by @Valid

        // Setup a explicitly invalid DTO state if needed, or if an empty object fails validation:
        FullImportDto invalidDto = new FullImportDto();
        // e.g., invalidDto.setSomeRequiredField(null);

        given()
                .contentType(ContentType.JSON)
                .body(invalidDto)
                .when()
                .post("/topics/import")
                .then()
                // Depending on how your validation exception mapper is configured,
                // Quarkus returns a 400 Bad Request by default for validation errors.
                .statusCode(400);

        Mockito.verifyNoInteractions(importService);
    }
}