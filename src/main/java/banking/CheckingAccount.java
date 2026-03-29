package banking;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

public class CheckingAccount extends BankAccount{
    private final long overdraftLimit;

    public CheckingAccount(String owner, long overdraftLimit) {
        super(owner);
        this.overdraftLimit = overdraftLimit;
    }

    @Override
    public void withdraw(long amount) {
        boolean isLocked = false;
        try {
            isLocked = getLock().tryLock(5, TimeUnit.SECONDS);
            if (!isLocked) throw new RuntimeException("Could not acquire lock");

            if (this.getBalance() - amount < -overdraftLimit) throw new IllegalStateException("Overdraft limit exceeded");
            balance -= amount;

            getTransactionHistory().add(
                    new Transaction(OffsetDateTime.now(),
                            TransactionType.WITHDRAW,
                            amount,
                            this,
                            null,
                            this.getBalance())
            );
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (isLocked) getLock().unlock();
        }
    }
}
