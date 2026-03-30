package banking;

import banking.exception.AccountNotExistException;

import java.util.List;
import java.util.UUID;

public interface AccountManager {
    BankAccount openAccount(String owner, AccountType type, Double interest, long dailyLimit,
                            CompoundingMode mode, Fee fee, Fee overdraftFee);
    void closeAccount(UUID accountNumber) throws AccountNotExistException;
    BankAccount findAccount(UUID accountNumber) throws AccountNotExistException;
    List<BankAccount> listAccounts();
}
