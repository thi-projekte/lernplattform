package de.thi.mynd.subscription.exception;

public class SubscriptionNotFoundException extends RuntimeException {
  public SubscriptionNotFoundException(String message) {
    super(message);
  }
}
