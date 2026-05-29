package de.thi.mynd.subscription.exception;

public class InvalidStripeSignatureException extends RuntimeException {
    public InvalidStripeSignatureException(String message) {
        super(message);
    }
}
