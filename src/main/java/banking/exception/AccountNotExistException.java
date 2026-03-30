package banking.exception;

public class AccountNotExistException extends RuntimeException {

    public AccountNotExistException() {
        super("Unknown or closed account");
    }
}
