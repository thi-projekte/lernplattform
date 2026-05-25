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
