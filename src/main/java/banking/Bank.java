package banking;

import banking.exception.AccountNotExistException;

import javax.swing.plaf.nimbus.State;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Bank implements AccountManager {
    private final ConcurrentHashMap<UUID, BankAccount> accounts;

    public Bank(ConcurrentHashMap<UUID, BankAccount> accounts) {
        this.accounts = accounts;
    }

    @Override
    public BankAccount openAccount(User owner, AccountType type, Double interestRate, long dailyLimit,
                                   CompoundingMode mode, Fee withdrawFee, Fee overdraftFee) {
        UUID id = UUID.randomUUID();

        BankAccount bankAccount = switch (type) {
            case AccountType.REGULAR -> new BankAccount(owner, dailyLimit, withdrawFee);
            case AccountType.SAVING -> new SavingAccount(owner, interestRate, dailyLimit, mode, withdrawFee);
            case AccountType.CHECKING -> new CheckingAccount(owner, dailyLimit, withdrawFee, overdraftFee);
        };
        this.accounts.putIfAbsent(id, bankAccount);
        owner.getAccounts().add(bankAccount);
        return bankAccount;
    }

    @Override
    public void closeAccount(UUID accountNumber) throws AccountNotExistException {
        BankAccount account = this.findAccount(accountNumber);
        this.accounts.remove(accountNumber);
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
        BankAccount account = this.findAccount(accountNumber);
        long startingBalance = account.getTransactionHistory().stream()
                .filter(transaction -> transaction.timestamp().isBefore(fromDate))
                .reduce((first, second) -> second)
                .map(Transaction::resultingBalance)
                .orElse(0L);

        long endingBalance = account.getTransactionHistory().stream()
                .filter(transaction -> !transaction.timestamp().isAfter(toDate))
                .reduce((first, second) -> second)
                .map(Transaction::resultingBalance)
                .orElse(0L);

        List<Transaction> transactions = account.getTransactionHistory().stream()
                .filter(transaction ->
                        !transaction.timestamp().isBefore(fromDate)
                        && !transaction.timestamp().isAfter(toDate)
                )
                .toList();
        return new Statement(startingBalance, endingBalance, transactions);
    }
}
