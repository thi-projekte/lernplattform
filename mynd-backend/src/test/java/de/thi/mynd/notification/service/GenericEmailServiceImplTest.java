package de.thi.mynd.notification.service;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.ext.mail.MailMessage;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class GenericEmailServiceImplTest {

  @Inject GenericEmailService emailService;

  @Inject MockMailbox mockMailbox; // Intercepts outgoing emails in test mode

  @BeforeEach
  void setup() {
    // Clear the mailbox before every individual test run
    // to prevent test contamination.
    mockMailbox.clear();
  }

  @Test
  void testSendEmail_Success() {
    // Arrange
    String templateName = "invitation.html"; // Assumes this exists in mail-templates/
    String subject = "Train your Brain!";
    List<String> recipients = List.of("test-user@mynd.com");

    Map<String, String> parameters =
        Map.of(
            "name", "Jane Doe",
            "logoUrl", "https://mynd.com/logo.png",
            "invitationLink", "https://mynd.com/invite/123");

    // Act
    emailService.sendEmail(templateName, subject, recipients, parameters);

    // Assert - Verify the message arrived in the mock inbox
    List<MailMessage> sentMessages = mockMailbox.getMailMessagesSentTo("test-user@mynd.com");
    assertEquals(1, sentMessages.size(), "Should have sent exactly 1 email to the recipient");

    MailMessage actualMail = sentMessages.get(0);
    assertEquals(subject, actualMail.getSubject());

    // Assert the dynamic template parameters rendered into the HTML correctly
    String htmlContent = actualMail.getHtml();
    assertNotNull(htmlContent);
    assertTrue(htmlContent.contains("Hello,"));
    assertTrue(htmlContent.contains("https://mynd.com/logo.png"));
    assertTrue(htmlContent.contains("https://mynd.com/invite/123"));
  }

  @Test
  void testSendEmail_MultipleRecipients() {
    // Arrange
    String templateName = "invitation.html";
    String subject = "Group Invitation";
    List<String> recipients = List.of("user1@mynd.com", "user2@mynd.com");
    Map<String, String> parameters =
        Map.of("name", "Team", "logoUrl", "...", "invitationLink", "...");

    // Act
    emailService.sendEmail(templateName, subject, recipients, parameters);

    // Assert - Check overall sent mailbox size
    assertEquals(2, mockMailbox.getTotalMessagesSent(), "Two separate deliveries should be logged");
    assertEquals(1, mockMailbox.getMailMessagesSentTo("user1@mynd.com").size());
    assertEquals(1, mockMailbox.getMailMessagesSentTo("user2@mynd.com").size());
  }

  @Test
  void testSendEmail_TemplateNotFound_ThrowsException() {
    // Arrange
    String invalidTemplate = "non-existent-file.html";
    List<String> recipients = List.of("error@mynd.com");
    Map<String, String> parameters = Map.of();

    // Act & Assert
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              emailService.sendEmail(invalidTemplate, "Error Test", recipients, parameters);
            });

    assertTrue(exception.getMessage().contains("Template not found"));
    assertEquals(
        0,
        mockMailbox.getTotalMessagesSent(),
        "No email should be broadcasted if compiling template fails");
  }
}
