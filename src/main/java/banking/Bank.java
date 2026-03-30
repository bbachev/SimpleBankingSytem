package banking;

import banking.exception.AccountNotExistException;
import banking.exception.CouldNotAcquireLockException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Bank implements AccountManager {
    private final ConcurrentHashMap<UUID, BankAccount> accounts;

    public Bank(ConcurrentHashMap<UUID, BankAccount> accounts) {
        this.accounts = accounts;
    }

    @Override
    public BankAccount openAccount(User owner, AccountType type, Double interestRate, long dailyLimit,
                                   long overdraftLimit, CompoundingMode mode, Fee withdrawFee, Fee overdraftFee) {
        UUID id = UUID.randomUUID();

        BankAccount bankAccount = switch (type) {
            case REGULAR -> new BankAccount(owner, dailyLimit, withdrawFee);
            case SAVING -> new SavingAccount(owner, interestRate, dailyLimit, mode, withdrawFee);
            case CHECKING -> new CheckingAccount(owner, dailyLimit, overdraftLimit, withdrawFee, overdraftFee);
        };
        this.accounts.putIfAbsent(id, bankAccount);
        owner.getAccounts().add(bankAccount);
        return bankAccount;
    }

    @Override
    public void closeAccount(UUID accountNumber) throws AccountNotExistException {
        BankAccount account = this.accounts.remove(accountNumber);
        if (account == null) throw new AccountNotExistException();
        account.getOwner().getAccounts().remove(account);

    }

    @Override
    public BankAccount findAccount(UUID accountNumber) throws AccountNotExistException {
        BankAccount bankAccount = this.accounts.get(accountNumber);
        if (bankAccount == null) throw new AccountNotExistException();
        return bankAccount;
    }

    @Override
    public List<BankAccount> listAccounts() {
        return this.accounts.values().stream().toList();
    }

    @Override
    public Statement generateStatement(UUID accountNumber, LocalDate fromDate, LocalDate toDate) {
        boolean isLocked = false;
        BankAccount account = this.findAccount(accountNumber);
        try {
            isLocked = account.getLock().tryLock(5, TimeUnit.SECONDS);
            if (!isLocked) throw new CouldNotAcquireLockException();

            long startingBalance = account.getTransactionHistory().stream()
                    .filter(transaction -> transaction.timestamp().toLocalDate().isBefore(fromDate))
                    .reduce((first, second) -> second)
                    .map(Transaction::resultingBalance)
                    .orElse(0L);

            long endingBalance = account.getTransactionHistory().stream()
                    .filter(transaction -> !transaction.timestamp().toLocalDate().isAfter(toDate))
                    .reduce((first, second) -> second)
                    .map(Transaction::resultingBalance)
                    .orElse(0L);

            List<Transaction> transactions = account.getTransactionHistory().stream()
                    .filter(transaction ->
                            !transaction.timestamp().toLocalDate().isBefore(fromDate)
                                    && !transaction.timestamp().toLocalDate().isAfter(toDate)
                    )
                    .toList();
            return new Statement(startingBalance, endingBalance, transactions);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (isLocked) account.getLock().unlock();
        }
    }
}
