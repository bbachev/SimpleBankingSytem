package banking;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

public class CheckingAccount extends BankAccount{
    private final long overdraftLimit;

    public CheckingAccount(String owner, long dailyLimit) {
        super(owner, dailyLimit);
        this.overdraftLimit = dailyLimit;
    }

    @Override
    public void withdraw(long amount) {
        boolean isLocked = false;
        try {
            isLocked = getLock().tryLock(5, TimeUnit.SECONDS);
            if (!isLocked) throw new RuntimeException("Could not acquire lock");

            resetDailyLimitIfNeeded();
            if (this.getBalance() - amount < -overdraftLimit) throw new IllegalStateException("Overdraft limit exceeded");

            if (getSpentToday() + amount > getDailyWithdrawalLimit())
                throw new IllegalArgumentException("Daily withdrawal limit exceeded");

            this.spentToday += amount;
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
