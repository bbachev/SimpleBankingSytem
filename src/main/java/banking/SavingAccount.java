package banking;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

public class SavingAccount extends BankAccount{
    private final Double interestRate;
    private long maxBalance;
    private final CompoundingMode mode;

    public SavingAccount(String owner, Double interestRate, long limit, CompoundingMode mode, Fee fee) {
        super(owner, limit, fee);
        this.interestRate = interestRate;
        this.maxBalance = limit;
        this.mode = mode;
    }


    public void applyInterest(){
        boolean isLocked = false;
        try {
            isLocked = getLock().tryLock(5, TimeUnit.SECONDS);
            if (!isLocked) throw new RuntimeException("Could not acquire lock");

            long currentBalance = getBalance();

            long interest = switch(mode) {
                case CompoundingMode.YEARLY ->  (long) (currentBalance * interestRate);
                case CompoundingMode.MONTHLY -> (long) (currentBalance * (interestRate) / 12);
            };
            
            if (getSpentToday() + interest > maxBalance) throw new IllegalArgumentException("Max balance could not be exceed");

            balance += interest;

            getTransactionHistory().add(
                            new Transaction(OffsetDateTime.now(),
                                    TransactionType.INTEREST,
                                    interest,
                                    this,
                                    null,
                                    this.getBalance())

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
            if (!isLocked) throw new RuntimeException("Could not acquire lock");

            long currentBalance = getBalance();
            if (currentBalance + amount > this.maxBalance ) throw new IllegalArgumentException("Max balance exceeded");
            super.deposit(amount);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (isLocked) getLock().unlock();
        }

    }
}
