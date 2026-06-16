/**
 * This file is part of the MYnd application (de.thi.mynd).
 *
 * <p>Copyright (c) 2026 THI Projekte
 *
 * <p>For the full copyright and license information, please view the LICENSE file that was
 * distributed with this source code.
 */
package de.thi.mynd.auth.service;

import de.thi.mynd.common.exception.UserNotFoundException;

public interface AuthService {

  boolean checkUserIsBuilder(String username) throws UserNotFoundException;

  void makeUserABuilder(String username) throws UserNotFoundException;

  void makeUserALearner(String username) throws UserNotFoundException;
}
