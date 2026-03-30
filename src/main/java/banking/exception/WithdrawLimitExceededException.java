package banking.exception;

public class WithdrawLimitExceededException extends RuntimeException {
    public WithdrawLimitExceededException() {
        super("Withdraw Limit exceeded");
    }
}
