package banking;

import banking.exception.CouldNotAcquireLockException;
import banking.exception.DailyWithdrawalLimitException;
import banking.exception.OverdraftLimitExceededException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

public class CheckingAccount extends BankAccount{
    private final long overdraftLimit;
    private final Fee overdraftFee;
    private LocalDate lastNegativeBalance = LocalDate.MIN;;

    public CheckingAccount(User owner, long dailyLimit, Fee withdrawFee, Fee overdraftFee) {
        super(owner, dailyLimit, withdrawFee);
        this.overdraftLimit = dailyLimit;
        this.overdraftFee = overdraftFee;
    }

    @Override
    public void withdraw(long amount) {
        boolean isLocked = false;
        try {
            isLocked = getLock().tryLock(5, TimeUnit.SECONDS);
            if (!isLocked) throw new CouldNotAcquireLockException();

            resetDailyLimitIfNeeded();
            if (this.getBalance() - amount < -overdraftLimit) throw new OverdraftLimitExceededException();

            if (getSpentToday() + amount > getDailyWithdrawalLimit())
                throw new DailyWithdrawalLimitException();

            long withdrawalFee = checkForWithdrawalFee() ? this.overdraftFee.amount(): 0;
            doWithdraw(amount, withdrawalFee);

            if(withdrawalFee > 0) {
                getTransactionHistory().add(
                        new Transaction(OffsetDateTime.now(),
                                TransactionType.FEE,
                                this.overdraftFee.amount(),
                                this,
                                null,
                                this.getBalance()
                        )
                );
            }


        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (isLocked) getLock().unlock();
        }
    }
    private boolean checkForWithdrawalFee(){
        if (getBalance() < 0 && !lastNegativeBalance.isEqual(LocalDate.now())){
            lastNegativeBalance = LocalDate.now();
            return true;
        }
        return false;
    }
}
