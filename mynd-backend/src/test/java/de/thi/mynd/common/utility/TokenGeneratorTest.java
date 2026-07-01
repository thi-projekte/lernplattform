/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.common.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class TokenGeneratorTest {

  @Test
  void constructor_isCallable() {
    assertNotNull(new TokenGenerator());
  }

  @Test
  void generateRandomString_returnsStringOfRequestedLength() {
    String token = TokenGenerator.generateRandomString(16);

    assertNotNull(token);
    assertEquals(16, token.length());
  }

  @Test
  void generateRandomString_consecutiveCalls_produceDifferentValues() {
    String first = TokenGenerator.generateRandomString(32);
    String second = TokenGenerator.generateRandomString(32);

    assertNotEquals(first, second);
  }
}
