package banking.exception;

public class CouldNotAcquireLockException extends RuntimeException{

    public CouldNotAcquireLockException() {
        super("Could not acquire lock");
    }
}
