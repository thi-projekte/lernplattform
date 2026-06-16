/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * Copyright (c) 2026 THI Projekte
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package de.thi.mynd.progressTracking.entity;

public enum LearnProgressStatus {
  STARTED("STARTED"),
  ALL_CONTENT_ELEMENTS_COMPLETED("ALL_CONTENT_ELEMENTS_COMPLETED"),
  COMPLETED_MANUALLY("COMPLETED_MANUALLY");

  public final String label;

  private LearnProgressStatus(String label) {
    this.label = label;
  }
}
