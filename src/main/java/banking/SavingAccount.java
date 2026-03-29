package banking;

public class SavingAccount extends BankAccount{
    private final Double interestRete;

    public SavingAccount(String owner, Double interestRete) {
        super(owner);
        this.interestRete = interestRete;
    }


    public synchronized void applyInterest(){
        long interest = (long) (super.getBalance() * interestRete);
        super.deposit(interest);
    }
}
