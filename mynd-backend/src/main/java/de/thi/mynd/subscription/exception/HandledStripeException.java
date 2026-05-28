package de.thi.mynd.subscription.exception;

public class HandledStripeException extends RuntimeException {
    public HandledStripeException(String message) {
        super(message);
    }
}
