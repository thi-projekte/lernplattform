package de.thi.mynd.notification.service;

import java.util.List;
import java.util.Map;

public interface GenericEmailService {

    void sendEmail(String template, String subject, List<String> recipients, Map<String, String> parameters);
}
