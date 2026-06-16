/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package de.thi.mynd.common.entity;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

public class LtreeType implements UserType<String> {

  @Override
  public int getSqlType() {
    return Types.OTHER;
  }

  @Override
  public Class<String> returnedClass() {
    return String.class;
  }

  @Override
  public void nullSafeSet(
      PreparedStatement st, String value, int index, SharedSessionContractImplementor session)
      throws SQLException {
    if (value == null) {
      st.setNull(index, Types.OTHER);
    } else {
      st.setObject(index, value, Types.OTHER);
    }
  }

  @Override
  public String nullSafeGet(
      ResultSet rs, int position, SharedSessionContractImplementor session, Object owner)
      throws SQLException {
    return rs.getString(position);
  }

  @Override
  public String deepCopy(String value) {
    return value;
  }

  @Override
  public boolean isMutable() {
    return false;
  }

  @Override
  public Serializable disassemble(String value) {
    return value;
  }

  @Override
  public String assemble(Serializable cached, Object owner) {
    return (String) cached;
  }

  @Override
  public boolean equals(String x, String y) {
    return Objects.equals(x, y);
  }

  @Override
  public int hashCode(String x) {
    return Objects.hashCode(x);
  }
}
