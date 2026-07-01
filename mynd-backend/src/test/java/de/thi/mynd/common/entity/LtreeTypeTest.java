/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.common.entity;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import java.sql.Types;
import org.junit.jupiter.api.Test;

@QuarkusTest
class LtreeTypeTest {

  private final LtreeType ltreeType = new LtreeType();

  @Test
  void getSqlType_returnsOther() {
    assertEquals(Types.OTHER, ltreeType.getSqlType());
  }

  @Test
  void returnedClass_isString() {
    assertEquals(String.class, ltreeType.returnedClass());
  }

  @Test
  void deepCopy_returnsSameValue() {
    assertEquals("root.child", ltreeType.deepCopy("root.child"));
  }

  @Test
  void isMutable_returnsFalse() {
    assertFalse(ltreeType.isMutable());
  }

  @Test
  void disassemble_returnsValueUnchanged() {
    assertEquals("root.child", ltreeType.disassemble("root.child"));
  }

  @Test
  void assemble_castsBackToString() {
    assertEquals("root.child", ltreeType.assemble("root.child", null));
  }

  @Test
  void equals_sameValues_returnsTrue() {
    assertTrue(ltreeType.equals("root.child", "root.child"));
  }

  @Test
  void equals_differentValues_returnsFalse() {
    assertFalse(ltreeType.equals("root.child", "root.other"));
  }

  @Test
  void hashCode_matchesObjectsHashCode() {
    assertEquals(java.util.Objects.hashCode("root.child"), ltreeType.hashCode("root.child"));
  }
}
