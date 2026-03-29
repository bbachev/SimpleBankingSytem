package banking;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Bank implements AccountManager {
    private final ConcurrentHashMap<> accounts;

    public Bank(ConcurrentHashMap accounts) {
        this.accounts = accounts;
    }

    @Override
    public BankAccount openAccount(UUID accountNumber, String owner, AccountType type) {
        UUID uuid = UUID.randomUUID();

        this.accounts.putIfAbsent()
    }

    @Override
    public void closeAccount(UUID accountNumber) {

    }

    @Override
    public Optional<BankAccount> findAccount(UUID accountNumber) {
        return Optional.empty();
    }

    @Override
    public List<BankAccount> listAccounts() {
        return List.of();
    }
}
