package banking;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountManager {
    BankAccount openAccount(UUID AccountNumber, String owner, AccountType type);
    void closeAccount(UUID accountNumber);
    Optional<BankAccount> findAccount(UUID accountNumber);
    List<BankAccount> listAccounts();
}
