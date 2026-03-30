package banking.exception;

public class InvalidAmountException extends RuntimeException {

    public InvalidAmountException() {
        super("Invalid amount. Amount must be positive number.");
    }
}
