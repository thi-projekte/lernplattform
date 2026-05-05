package de.thi.mynd.auth.service;

import de.thi.mynd.auth.dto.RegisterUserRequestDto;
import de.thi.mynd.auth.exception.UserAlreadyExistsException;

public interface AuthService {

    boolean checkUsernameExists(String username);

    void registerUser(RegisterUserRequestDto requestDto) throws UserAlreadyExistsException;
}
