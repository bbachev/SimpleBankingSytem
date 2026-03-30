package banking.exception;

public class OverdraftLimitExceededException extends RuntimeException {
    public OverdraftLimitExceededException() {
        super("Overdraft limit exceeded");
    }
}
