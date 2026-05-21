package de.thi.mynd.notification.service;

import io.quarkus.logging.Log;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public final class GenericEmailServiceImpl implements GenericEmailService {

  @Inject Engine quteEngine;

  @Inject Mailer mailer;

  @Override
  public void sendEmail(
      String templateName,
      String subject,
      List<String> recipients,
      Map<String, String> parameters) {
    Template template = quteEngine.getTemplate(templateName);

    if (template == null) {
      throw new IllegalArgumentException("Template not found: " + templateName);
    }

    String renderedHtml = template.instance().data(parameters).render();

    Mail mail = new Mail();
    mail.setTo(recipients);
    mail.setSubject(subject);
    mail.setHtml(renderedHtml);

    mailer.send(mail);

    Log.infof(
        "Sent email with subject %s to %s of template %s",
        subject, String.join(", ", recipients), templateName);
  }
}
