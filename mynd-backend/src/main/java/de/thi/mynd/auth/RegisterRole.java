/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package de.thi.mynd.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum RegisterRole {
  @JsonProperty("builder")
  Builder,

  @JsonProperty("admin")
  Admin;

  @JsonCreator
  public static RegisterRole fromString(String value) {
    if (value == null) return null;
    return RegisterRole.valueOf(
        value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase());
  }
}
