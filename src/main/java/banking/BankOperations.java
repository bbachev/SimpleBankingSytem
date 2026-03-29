package banking;

public interface BankOperations {
    void deposit(long amount);
    void withdraw(long amount);
    long getBalance();
    void transferTo(BankAccount otherAccount, long amount);
}
