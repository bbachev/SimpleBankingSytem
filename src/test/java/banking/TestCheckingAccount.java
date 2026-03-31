package banking;

import banking.exception.DailyWithdrawalLimitException;
import banking.exception.OverdraftLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

public class TestCheckingAccount {
    private CheckingAccount account;
    private static final User USER  = new User(UUID.randomUUID(), "John", "Doe");
    private static final Fee WITHDROW_FEE = new Fee(FeeType.WITHDRAWAL_FEE, 3L);
    private static final Fee OVERDRAFT_FEE = new Fee(FeeType.WITHDRAWAL_FEE, 2L);

    @BeforeEach
    public void setUp() {
        account = new CheckingAccount(USER, 2000, 900, WITHDROW_FEE, OVERDRAFT_FEE);
    }

    @Test
    public void testWithdrawMoreThanOverdraftLimitShouldThrow() {
        account.deposit(900);

        assertEquals(900, account.getBalance());
        assertThrowsExactly(OverdraftLimitExceededException.class, () -> account.withdraw(1801));
    }

    @Test
    public void testDailyWithdrawalLimitExceededShouldThrow() {
        account.deposit(3000);

        assertEquals(3000, account.getBalance());
        assertThrowsExactly(DailyWithdrawalLimitException.class, () -> account.withdraw(2001));
    }

    @Test
    public void testOverdraftFeeAppliedOncePerDay() {
        account.deposit(500);

        account.withdraw(600);
        account.deposit(500);
        account.withdraw(450);

        assertEquals(500 + 500 - 450 - 600 - OVERDRAFT_FEE.amount() - 2 * WITHDROW_FEE.amount(),  account.getBalance());
    }
}
