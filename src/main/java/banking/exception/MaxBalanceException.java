package banking.exception;

public class MaxBalanceException extends RuntimeException {
    public MaxBalanceException() {
        super("Max balance could not be exceeded");
    }
}
