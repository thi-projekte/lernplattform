package de.thi.mynd.auth.exception;

public class NoInvitationsLeftException extends RuntimeException {
  public NoInvitationsLeftException(String message) {
    super(message);
  }
}
