package banking;

import banking.exception.InvalidAmountException;
import banking.exception.MaxBalanceException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
public class TestSavingAccount {
    private SavingAccount account;
    private static final User USER  = new User(UUID.randomUUID(), "John", "Doe");
    private static final Fee WITHDROW_FEE = new Fee(FeeType.WITHDRAWAL_FEE, 3L);

    @Test
    public void testApplyInterestNegativeOrZeroInterestShouldThrow() {
        account = new SavingAccount(USER, -3.0, 1000, CompoundingMode.MONTHLY, WITHDROW_FEE);
        assertThrowsExactly(InvalidAmountException.class, () -> account.applyInterest());

        account = new SavingAccount(USER, 0D, 1000, CompoundingMode.MONTHLY, WITHDROW_FEE);
        assertThrowsExactly(InvalidAmountException.class, () -> account.applyInterest());
    }

    @Test
    public void testApplyInterestOverMaxBalanceShouldThrow() {
        account = new SavingAccount(USER, 500.0, 1000, CompoundingMode.YEARLY, WITHDROW_FEE);
        account.deposit(1000);

        assertThrowsExactly(MaxBalanceException.class, () -> account.applyInterest());

        account = new SavingAccount(USER, 500.0, 1000, CompoundingMode.MONTHLY, WITHDROW_FEE);
        account.deposit(1000);
        assertThrowsExactly(MaxBalanceException.class, () -> account.applyInterest());
    }

    @Test
    public void testApplyInterestMonthlyShouldBeCorrect () {
        account = new SavingAccount(USER, 500.0, 10000, CompoundingMode.MONTHLY, WITHDROW_FEE);
        account.deposit(1200);
        account.applyInterest();

        assertEquals(1205, account.getBalance());
    }

    @Test
    public void testApplyInterestYearlyShouldBeCorrect () {
        account = new SavingAccount(USER, 500.0, 10000, CompoundingMode.YEARLY, WITHDROW_FEE);
        account.deposit(1200);
        account.applyInterest();

        assertEquals(1260, account.getBalance());
    }
}
