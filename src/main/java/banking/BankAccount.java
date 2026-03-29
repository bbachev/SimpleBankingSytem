package banking;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@AllArgsConstructor
@Getter
public class BankAccount implements BankOperations{
    private final UUID accountNumber;
    private final String owner;
    private long balance;
    private ReentrantLock reentrantLock;

    @Override
    public void deposit(long amount) {
        boolean isLocked = false;

        if (amount < 0) throw new IllegalArgumentException("Amount cannot be negative");
        try {
            isLocked = reentrantLock.tryLock(5, TimeUnit.SECONDS);
            this.balance += amount;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (isLocked) reentrantLock.unlock();
        }

    }

    @Override
    public void withdraw(long amount) {
        if (this.getBalance() < amount) throw new IllegalArgumentException("Not enough balance");
        if (amount < 0) throw new IllegalArgumentException("Amount cannot be negative");

        boolean isLocked = false;
        try {
            isLocked = reentrantLock.tryLock(5, TimeUnit.SECONDS);
            this.balance -= amount;
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

            if (!isFirstLocked || !isSecondLocked) throw new IllegalArgumentException("One of the lock cannot be acquired");

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
    }

    public ReentrantLock getLock() {
        return reentrantLock;
    }
}
