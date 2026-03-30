package banking;

import banking.exception.CouldNotAcquireLockException;
import banking.exception.InvalidAmountException;
import banking.exception.MaxBalanceException;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public class SavingAccount extends BankAccount{
    private final Double interestRate;
    private long maxBalance;
    private final CompoundingMode mode;

    public SavingAccount(User owner, Double interestRate, long limit, CompoundingMode mode, Fee fee) {
        super(owner, limit, fee);
        this.interestRate = interestRate;
        this.maxBalance = limit;
        this.mode = mode;
    }


    public void applyInterest() {
        boolean isLocked = false;
        if (this.interestRate <= 0) throw new InvalidAmountException();

        try {
            isLocked = getLock().tryLock(5, TimeUnit.SECONDS);
            if (!isLocked) throw new CouldNotAcquireLockException();

            long currentBalance = this.balanceUnsafe();

            long interest = switch(mode) {
                case CompoundingMode.YEARLY ->  (long) (currentBalance * interestRate) / 10000;
                case CompoundingMode.MONTHLY -> (long) (currentBalance * interestRate) / 10000 / 12;
            };
            
            if (currentBalance + interest > maxBalance) throw new MaxBalanceException();

            this.addToBalance(interest);

            getTransactionHistory().add(
                            new Transaction(LocalDateTime.now(),
                                    TransactionType.INTEREST,
                                    interest,
                                    this,
                                    null,
                                    this.balanceUnsafe())

            );
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        finally {
            if (isLocked) getLock().unlock();
        }

    }

    @Override
    public void deposit(long amount) {
        boolean isLocked = false;

        try{
            isLocked = getLock().tryLock(5, TimeUnit.SECONDS);
            if (!isLocked) throw new CouldNotAcquireLockException();

            long currentBalance = this.balanceUnsafe();
            if (currentBalance + amount > this.maxBalance ) throw new MaxBalanceException();
            this.depositUnsafe(amount);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (isLocked) getLock().unlock();
        }

    }
}
