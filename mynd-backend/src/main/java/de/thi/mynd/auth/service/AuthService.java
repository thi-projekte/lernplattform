package de.thi.mynd.auth.service;

import de.thi.mynd.common.exception.UserNotFoundException;

public interface AuthService {

  boolean checkUserIsBuilder(String username) throws UserNotFoundException;

  void makeUserABuilder(String username) throws UserNotFoundException;

  void makeUserALearner(String username) throws UserNotFoundException;
}
