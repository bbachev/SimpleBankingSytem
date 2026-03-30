package banking;

import banking.exception.*;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Getter
public class BankAccount implements BankOperations {
    private final User owner;
    private long balance = 0;
    protected long spentToday = 0;
    private LocalDate lastTrackedDate;
    private final long dailyWithdrawalLimit;
    private final Fee widtrawalFee;


    private ReentrantLock reentrantLock = new ReentrantLock();
    private final List<Transaction> transactionHistory = new ArrayList<>();

    public BankAccount(User owner, long dailyLimit, Fee widtrawalFee) {
        this.owner = owner;
        this.dailyWithdrawalLimit = dailyLimit;
        this.widtrawalFee = widtrawalFee;
        this.lastTrackedDate = LocalDate.now();
    }

    @Override
    public void deposit(long amount) {
        boolean isLocked = false;

        if (amount <= 0) throw new InvalidAmountException();
        try {
            isLocked = reentrantLock.tryLock(5, TimeUnit.SECONDS);
            if (!isLocked) throw new CouldNotAcquireLockException();
            this.depositUnsafe(amount);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (isLocked) reentrantLock.unlock();
        }
    }

    protected void doWithdraw(long amount, long additionalFee) {
            long total = amount + this.widtrawalFee.amount() + additionalFee;
            this.balance -= total;
            this.spentToday += amount;

            transactionHistory.add(
                    new Transaction(LocalDateTime.now(),
                            TransactionType.WITHDRAW,
                            amount,
                            this,
                            null,
                            this.balance)
            );

            transactionHistory.add(
                    new Transaction(LocalDateTime.now(),
                            TransactionType.FEE,
                            this.widtrawalFee.amount(),
                            this,
                            null,
                            this.balance
                    )
            );
    }
        @Override
    public void withdraw(long amount) {
        if (amount < 0) throw new InvalidAmountException();

        boolean isLocked = false;
        try {
            isLocked = reentrantLock.tryLock(5, TimeUnit.SECONDS);
            if (!isLocked) throw new CouldNotAcquireLockException();

            if (this.balance < amount) throw new InsufficientFundsException();

            resetDailyLimitIfNeeded();
            long spentToday = this.spentToday;

            if (spentToday > dailyWithdrawalLimit || spentToday + amount > dailyWithdrawalLimit)
                throw new WithdrawLimitExceededException();

            doWithdraw(amount, 0);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (isLocked) reentrantLock.unlock();
        }
    }

    @Override
    public long getBalance() {
        boolean isLocked = false;

        try {
            isLocked = reentrantLock.tryLock(5, TimeUnit.SECONDS);
            if (!isLocked) throw new CouldNotAcquireLockException();
            return this.balance;

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (isLocked) reentrantLock.unlock();
        }
    }

    @Override
    public void transferTo(BankAccount otherAccount, long amount) {
        if (otherAccount == null) throw new AccountNotExistException();

        boolean isFirstLocked = false;
        boolean isSecondLocked = false;

        ReentrantLock firstLock = null;
        ReentrantLock secondLock = null;

        long currentBalance = 0;
        long otherAccountBalance = 0;

        try {
            firstLock = this.owner.getId().compareTo(otherAccount.getOwner().getId()) < 0
                    ? this.reentrantLock : otherAccount.getLock();
            secondLock = this.owner.getId().compareTo(otherAccount.getOwner().getId()) < 0
                    ? otherAccount.getLock() : this.reentrantLock;

            isFirstLocked = firstLock.tryLock(5, TimeUnit.SECONDS);
            isSecondLocked = secondLock.tryLock(5, TimeUnit.SECONDS);

            if (!isFirstLocked || !isSecondLocked)
                throw new CouldNotAcquireLockException();

            currentBalance = this.balance;
            otherAccountBalance = otherAccount.balance;

            this.withdraw(amount);
            otherAccount.deposit(amount);

        } catch (InterruptedException e) {
            throw new RuntimeException();
        } catch (Exception e) {
            if (this.getBalance() != currentBalance) {
                this.balance = currentBalance;
            }
            if (otherAccount.getBalance() != otherAccountBalance) {
               otherAccount.balance = otherAccountBalance;
            }
            throw new RuntimeException(e);

        } finally {
            if (isFirstLocked) firstLock.unlock();
            if (isSecondLocked) secondLock.unlock();
        }

        transactionHistory.add(
                new Transaction(LocalDateTime.now(),
                        TransactionType.TRANSFER,
                        amount,
                        this,
                        otherAccount,
                        balance)
        );
    }

    public ReentrantLock getLock() {
        return reentrantLock;
    }

    public long getSpentToday(){

        boolean isLocked = false;

        try {
            isLocked = this.reentrantLock.tryLock(5, TimeUnit.SECONDS);
            if (!isLocked) throw new CouldNotAcquireLockException();

            return this.spentToday;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (isLocked) this.reentrantLock.unlock();
        }
    }
    protected void resetDailyLimitIfNeeded() {
        if (!this.lastTrackedDate.isEqual(LocalDate.now())) {
            lastTrackedDate = LocalDate.now();
            this.spentToday = 0;
        }
    }
    protected void addToBalance(long amount) {
        this.balance += amount;
    }
    protected long balanceUnsafe() {
        return this.balance;
    }
    protected void depositUnsafe(long amount) {
        this.balance += amount;
        transactionHistory.add(
                new Transaction(LocalDateTime.now(),
                        TransactionType.DEPOSIT,
                        amount,
                        this,
                        null,
                        this.balance)
        );
    }
}
