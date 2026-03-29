package banking;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

public class SavingAccount extends BankAccount{
    private final Double interestRate;

    public SavingAccount(String owner, Double interestRate) {
        super(owner);
        this.interestRate = interestRate;
    }


    public void applyInterest(){
        boolean isLocked = false;
        try {
            isLocked = getLock().tryLock(5, TimeUnit.SECONDS);
            if (!isLocked) throw new RuntimeException("Could not acquire lock");

            long interest = (long) (getBalance() * interestRate);
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
}
