package banking.exception;

public class InvalidAmountException extends Exception{

    public InvalidAmountException() {
        super("Invalid amount. Amount must be positive number.");
    }
}
