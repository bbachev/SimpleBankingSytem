package banking.exception;

public class DailyWithdrawalLimitException extends RuntimeException {
    public DailyWithdrawalLimitException() {
        super("Overdraft limit exceeded");
    }
}
