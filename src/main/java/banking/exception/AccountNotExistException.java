package banking.exception;

public class AccountNotExistException extends Exception{

    public AccountNotExistException() {
        super("Account doesn't exist");
    }
}
