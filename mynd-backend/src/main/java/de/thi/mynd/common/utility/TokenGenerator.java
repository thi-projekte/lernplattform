/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.common.utility;

import java.security.SecureRandom;
import java.util.stream.Collectors;

public final class TokenGenerator {

  private static final String CHARACTERS =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

  public static String generateRandomString(int length) {
    return new SecureRandom()
        .ints(length, 0, CHARACTERS.length())
        .mapToObj(CHARACTERS::charAt)
        .map(Object::toString)
        .collect(Collectors.joining());
  }
}
