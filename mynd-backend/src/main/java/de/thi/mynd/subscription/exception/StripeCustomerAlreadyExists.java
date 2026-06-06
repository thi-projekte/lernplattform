package de.thi.mynd.subscription.exception;

public class StripeCustomerAlreadyExists extends RuntimeException {
  public StripeCustomerAlreadyExists(String message) {
    super(message);
  }
}
