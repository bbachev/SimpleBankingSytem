package banking;

import lombok.Getter;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Getter
public class BankAccount implements BankOperations {
    private final String owner;
    protected long balance = 0;
    protected long spentToday = 0;
    private LocalDate lastTrackedDate;
    private final long dailyWithdrawalLimit;
    private final Fee widtrawalFee;


    private ReentrantLock reentrantLock = new ReentrantLock();
    private final List<Transaction> transactionHistory = new CopyOnWriteArrayList<>();

    public BankAccount(String owner, long dailyLimit, Fee widtrawalFee) {
        this.owner = owner;
        this.dailyWithdrawalLimit = dailyLimit;
        this.widtrawalFee = widtrawalFee;
        this.lastTrackedDate = LocalDate.now();
    }

    @Override
    public void deposit(long amount) {
        boolean isLocked = false;

        if (amount < 0) throw new IllegalArgumentException("Amount cannot be negative");
        try {
            isLocked = reentrantLock.tryLock(5, TimeUnit.SECONDS);
            if (!isLocked) throw new RuntimeException("Could not acquire lock");
            this.balance += amount;

            transactionHistory.add(
                    new Transaction(OffsetDateTime.now(),
                            TransactionType.DEPOSIT,
                            amount,
                            this,
                            null,
                            this.getBalance())
            );

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (isLocked) reentrantLock.unlock();
        }
    }

    protected void doWithdraw(long amount) {
        boolean isLocked = false;
        try {
            isLocked = reentrantLock.tryLock(5, TimeUnit.SECONDS);
            if (!isLocked) throw new RuntimeException("Could not acquire lock");

            long total = amount + this.widtrawalFee.amount();
            this.balance -= total;
            this.spentToday += amount;

            transactionHistory.add(
                    new Transaction(OffsetDateTime.now(),
                            TransactionType.WITHDRAW,
                            amount,
                            this,
                            null,
                            this.getBalance())
            );

            transactionHistory.add(
                    new Transaction(OffsetDateTime.now(),
                            TransactionType.FEE,
                            this.widtrawalFee.amount(),
                            this,
                            null,
                            this.getBalance()
                    )
            );

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }  finally {
            if (isLocked) reentrantLock.unlock();
        }
    }
        @Override
    public void withdraw(long amount) {
        if (amount < 0) throw new IllegalArgumentException("Amount cannot be negative");

        boolean isLocked = false;
        try {
            isLocked = reentrantLock.tryLock(5, TimeUnit.SECONDS);
            if (!isLocked) throw new RuntimeException("Could not acquire lock");

            if (this.getBalance() < amount) throw new IllegalArgumentException("Not enough balance");

            resetDailyLimitIfNeeded();
            long spentToday = this.getSpentToday();

            if (spentToday > dailyWithdrawalLimit || spentToday + amount > dailyWithdrawalLimit)
                throw new IllegalArgumentException("Withdraw Limit exceeded");

            doWithdraw(amount);

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
            if (!isLocked) throw new RuntimeException("Could not acquire lock");
            return this.balance;

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (isLocked) reentrantLock.unlock();
        }
    }

    @Override
    public void transferTo(BankAccount otherAccount, long amount) {
        if (otherAccount == null) throw new IllegalArgumentException("Other account cannot be null");

        boolean isFirstLocked = false;
        boolean isSecondLocked = false;

        ReentrantLock firstLock = null;
        ReentrantLock secondLock = null;

        long currentBalance = 0;
        long otherAccountBalance = 0;

        try {
            firstLock = this.owner.compareTo(otherAccount.getOwner()) < 0
                    ? this.reentrantLock : otherAccount.getLock();
            secondLock = this.owner.compareTo(otherAccount.getOwner()) < 0
                    ? otherAccount.getLock() : this.reentrantLock;

            isFirstLocked = firstLock.tryLock(5, TimeUnit.SECONDS);
            isSecondLocked = secondLock.tryLock(5, TimeUnit.SECONDS);

            if (!isFirstLocked || !isSecondLocked)
                throw new IllegalArgumentException("One of the lock cannot be acquired");

            currentBalance = this.getBalance();
            otherAccountBalance = otherAccount.getBalance();

            this.withdraw(amount);
            otherAccount.deposit(amount);

        } catch (InterruptedException e) {
            throw new RuntimeException();
        } catch (Exception e) {
            if (this.getBalance() != currentBalance) {
                this.deposit(amount);
            }
            if (otherAccount.getBalance() != otherAccountBalance) {
                otherAccount.withdraw(amount);
            }
            throw new RuntimeException(e);

        } finally {
            if (isFirstLocked) firstLock.unlock();
            if (isSecondLocked) secondLock.unlock();
        }

        transactionHistory.add(
                new Transaction(OffsetDateTime.now(),
                        TransactionType.TRANSFER,
                        amount,
                        this,
                        otherAccount,
                        this.getBalance())
        );
    }

    public ReentrantLock getLock() {
        return reentrantLock;
    }

    public long getSpentToday(){

        boolean isLocked = false;

        try {
            isLocked = this.reentrantLock.tryLock(5, TimeUnit.SECONDS);
            if (!isLocked) throw new RuntimeException("Could not acquire lock");

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
}
