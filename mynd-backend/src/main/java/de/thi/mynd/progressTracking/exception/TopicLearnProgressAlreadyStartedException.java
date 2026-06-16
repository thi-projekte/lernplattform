/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.progressTracking.exception;

public class TopicLearnProgressAlreadyStartedException extends RuntimeException {
  public TopicLearnProgressAlreadyStartedException(String message) {
    super(message);
  }
}
