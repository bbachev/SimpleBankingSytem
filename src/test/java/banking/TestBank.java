package banking;

import banking.exception.AccountNotExistException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class TestBank {
    private Bank bank = new Bank();
    private static final User USER = new User(UUID.randomUUID(), "John", "Doe");
    private static final Fee FEE = new Fee(FeeType.WITHDRAWAL_FEE,5L);

    @Test
    public void testOpenBankAccountShouldBeCorrect() {
        BankAccount bankAccount = bank.openAccount(USER, AccountType.REGULAR, 0D,
                1000, 0, null, FEE, null);

        assertNotNull(bankAccount);
        assertEquals(BankAccount.class, bankAccount.getClass());
        assertEquals( USER.getId(), bankAccount.getOwner().getId());
        assertEquals(1, bank.listAccounts().size());
        assertTrue(USER.getAccounts().contains(bankAccount));
    }

    @Test
    public void testCloseAccountShouldBeCorrect() {
        BankAccount bankAccount = bank.openAccount(USER, AccountType.REGULAR, 0D,
                1000, 0, null, FEE, null);
        assertNotNull(bankAccount);
        assertEquals(BankAccount.class, bankAccount.getClass());
        assertEquals( USER.getId(), bankAccount.getOwner().getId());

        bank.closeAccount(bankAccount.getId());

        assertThrowsExactly(AccountNotExistException.class, () -> bank.findAccount(bankAccount.getId()));
        assertFalse(USER.getAccounts().contains(bankAccount));
    }

    @Test
    public void testFindAccountNotExistShouldThrow() {
        assertEquals(0, bank.listAccounts().size());
        assertThrowsExactly(AccountNotExistException.class, () -> bank.findAccount(UUID.randomUUID()));
    }

    @Test
    public void testGenerateStatement () {
        BankAccount bankAccount = bank.openAccount(USER, AccountType.REGULAR, 0D,
                1000, 0, null, FEE, null);

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate twoDaysAgo = today.minusDays(2);

        bankAccount.addTransaction(new Transaction(
                twoDaysAgo.atStartOfDay(), TransactionType.DEPOSIT, 500, bankAccount, null, 500));

        bankAccount.addTransaction(new Transaction(
                yesterday.atStartOfDay(), TransactionType.DEPOSIT, 200, bankAccount, null, 700));
        bankAccount.addTransaction(new Transaction(
                today.atStartOfDay(), TransactionType.WITHDRAW, 100, bankAccount, null, 600));

        Statement statement = bank.generateStatement(bankAccount.getId(), yesterday, today);

        assertEquals(500, statement.startingBalance());

        assertEquals(600, statement.EndingBalance());

        assertEquals(2, statement.transactions().size());
        assertEquals(TransactionType.DEPOSIT, statement.transactions().get(0).type());
        assertEquals(TransactionType.WITHDRAW, statement.transactions().get(1).type());
    }

}
