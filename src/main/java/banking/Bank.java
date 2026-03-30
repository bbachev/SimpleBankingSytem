package banking;

import banking.exception.AccountNotExistException;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Bank implements AccountManager {
    private final ConcurrentHashMap<UUID, BankAccount> accounts;

    public Bank(ConcurrentHashMap<UUID, BankAccount> accounts) {
        this.accounts = accounts;
    }

    @Override
    public BankAccount openAccount(String owner, AccountType type, Double interestRate, long dailyLimit) {
        UUID id = UUID.randomUUID();

        BankAccount bankAccount = switch (type) {
            case AccountType.REGULAR -> new BankAccount(owner, dailyLimit);
            case AccountType.SAVING -> new SavingAccount(owner, interestRate, dailyLimit);
            case AccountType.CHECKING -> new CheckingAccount(owner, dailyLimit);
        };
        this.accounts.putIfAbsent(id, bankAccount);

        return bankAccount;
    }

    @Override
    public void closeAccount(UUID accountNumber) throws AccountNotExistException {
        this.findAccount(accountNumber);
        this.accounts.remove(accountNumber);
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
}
