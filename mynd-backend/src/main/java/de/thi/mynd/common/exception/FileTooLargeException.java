package de.thi.mynd.common.exception;

public class FileTooLargeException extends RuntimeException {
  public FileTooLargeException(String message) {
    super(message);
  }
}