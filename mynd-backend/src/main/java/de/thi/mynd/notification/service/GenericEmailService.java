/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package de.thi.mynd.notification.service;

import java.util.List;
import java.util.Map;

public interface GenericEmailService {

  void sendEmail(
      String template, String subject, List<String> recipients, Map<String, String> parameters);
}
