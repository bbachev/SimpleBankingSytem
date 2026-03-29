package banking;

public class CheckingAccount extends BankAccount{
    private final long overdraftLimit;

    public CheckingAccount(String owner, long overdraftLimit) {
        super(owner);
        this.overdraftLimit = overdraftLimit;
    }

    @Override
    public synchronized void withdraw(long amount) {
        if (this.getBalance() - amount < -overdraftLimit) throw new IllegalStateException("Overdraft limit exceeded");
        balance -= amount;
    }
}
