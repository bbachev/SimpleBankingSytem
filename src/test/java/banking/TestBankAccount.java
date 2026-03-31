package banking;
import banking.exception.AccountNotExistException;
import banking.exception.InsufficientFundsException;
import banking.exception.InvalidAmountException;
import banking.exception.WithdrawLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class TestBankAccount {
    private BankAccount accountFrom;
    private BankAccount accountTo;
    private static final User USER_A = new User(UUID.randomUUID(), "John", "Doe");
    private static final User USER_B = new User(UUID.randomUUID(), "Bob", "Jones");
    private static final int NUMBER_OF_THREADS = 50;

    private static final Fee FEE = new Fee(FeeType.WITHDRAWAL_FEE,5L);

    @BeforeEach
    void setUp() {
        this.accountFrom = new BankAccount(USER_A, 1000L, FEE);
        this.accountTo = new BankAccount(USER_B, 2000, FEE);
    }

    @Test
    public void testDepositIncreasesAmount(){
        assertEquals(0, accountFrom.getBalance());

        accountFrom.deposit(1000);
        accountFrom.deposit(200);

        assertEquals(1200, accountFrom.getBalance());
    }

    @Test
    public void testNegativeBalanceShouldThrow() {
        assertEquals(0, accountFrom.getBalance());
        assertThrowsExactly(InvalidAmountException.class, () -> accountFrom.deposit(-34));
    }

    @Test
    public void testWithdrawDecreasesAmount() {
        accountFrom.deposit(1200);

        accountFrom.withdraw(700);
        assertEquals(495, 1200 - 700 - FEE.amount(), accountFrom.getBalance());
    }

    @Test
    public void testWithdrawWhenBalanceGoesNegativeShouldThrow() {
        assertEquals(0, accountFrom.getBalance());

        accountFrom.deposit(700);
        assertThrowsExactly(InsufficientFundsException.class, () -> accountFrom.withdraw(701));
    }

    @Test
    public void testSpentTodayShouldShowActualValue(){
        assertEquals(0, accountFrom.getBalance());

        accountFrom.deposit(500);
        accountFrom.withdraw(300);
        accountFrom.withdraw(150);

        assertEquals(450, accountFrom.getSpentToday());
    }

    @Test
    public void testSpentTodayResetLimitShouldResetLimit(){
        assertEquals(0, accountFrom.getSpentToday());

        accountFrom.deposit(1000);
        accountFrom.withdraw(300);
        accountFrom.withdraw(250);

        assertEquals(550, accountFrom.getSpentToday());
        accountFrom.setLastTrackedDate(LocalDate.now().minusDays(1));

        accountFrom.withdraw(50);
        assertEquals(50, accountFrom.getSpentToday());

    }

    @Test
    public void testSpendTodayIsMoreThanWithdrawLimitShouldFail(){
        assertEquals(0, accountFrom.getSpentToday());

        accountFrom.deposit(2000);
        assertThrowsExactly(WithdrawLimitExceededException.class, () -> accountFrom.withdraw(1200));

        accountFrom.withdraw(900);
        assertThrowsExactly(WithdrawLimitExceededException.class, () -> accountFrom.withdraw(101));
    }

    @Test
    public void testTransferToShouldTransfer(){
        accountFrom.deposit(500);
        assertEquals(500, accountFrom.getBalance());

        accountFrom.transferTo(accountTo, 300);
        assertEquals(195, 500 - 300 - FEE.amount(), accountFrom.getBalance());
        assertEquals(300, accountTo.getBalance());
    }

    @Test
    public void testTransferToShouldRollback(){
        accountFrom.deposit(500);
        assertEquals(500, accountFrom.getBalance());

        assertThrows(Exception.class, () -> accountFrom.transferTo(accountTo, 501));

        assertEquals(500, accountFrom.getBalance());
        assertEquals(0, accountTo.getBalance());
    }

    @Test
    public void testDepositZeroShouldThrow(){
        assertThrowsExactly(InvalidAmountException.class, () -> accountFrom.deposit(0));
    }

    @Test
    public void testTransferToNullShouldThrow() {
        assertThrowsExactly(AccountNotExistException.class, () -> accountFrom.transferTo(null, 200));
    }

    @Test
    public void testTransferInsufficientFundsShouldThrow() {
        accountFrom.deposit(200);
        assertThrowsExactly(InsufficientFundsException.class, () -> accountFrom.transferTo(accountTo, 250));
    }

    @Test
    public void testConcurrentWithdraw() throws InterruptedException {
        accountFrom.deposit(1000);

        ExecutorService executorService = Executors.newFixedThreadPool(50);
        CountDownLatch countDownLatch = new CountDownLatch(NUMBER_OF_THREADS);

        for(int i = 0; i < NUMBER_OF_THREADS; i++) {
            executorService.submit(() -> {
                try{
                    accountFrom.withdraw(10);
                }finally {
                    countDownLatch.countDown();
                }
            });

        }
        countDownLatch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();
        assertEquals(500, (NUMBER_OF_THREADS * 10) + (NUMBER_OF_THREADS * FEE.amount()), accountFrom.getBalance());
    }

    @Test
    public void testConcurrentDeposit() throws InterruptedException {

        ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        CountDownLatch latch = new CountDownLatch(50);

        for(int i = 0; i < NUMBER_OF_THREADS; i++){
            executorService.submit(() -> {
                try{
                    executorService.submit(() -> accountFrom.deposit(20));
                }finally {
                    latch.countDown();
                }
            });

        }

        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        assertEquals(1000, NUMBER_OF_THREADS *  20, accountFrom.getBalance());
    }

    @Test
    public void testConcurrentBalanceShouldNotGoBelowZero() throws InterruptedException {
        accountFrom.deposit(1000);

        ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        CountDownLatch latch = new CountDownLatch(NUMBER_OF_THREADS);
        AtomicInteger exceptionCount = new AtomicInteger(0);
        AtomicInteger successCount = new AtomicInteger(0);

        for(int i = 0; i < NUMBER_OF_THREADS;  i++){
            executorService.submit(() -> {
                try{
                    accountFrom.withdraw(25);
                    successCount.incrementAndGet();
                } catch (RuntimeException e) {
                    exceptionCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });

        }
        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        assertTrue(accountFrom.getBalance() >= 0);
        assertEquals(NUMBER_OF_THREADS, exceptionCount.get() + successCount.get());
    }

    @Test
    public void testConcurrentWithdrawShouldThrowWhenLimitIsExceeded() throws InterruptedException {
        accountFrom.deposit(2000);

        ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        CountDownLatch latch = new CountDownLatch(NUMBER_OF_THREADS);
        AtomicInteger exceptionCount = new AtomicInteger(0);
        AtomicInteger successCount = new AtomicInteger(0);

        for(int i = 0; i < NUMBER_OF_THREADS;  i++){
            executorService.submit(() -> {
                try{
                    accountFrom.withdraw(25);
                    successCount.incrementAndGet();
                } catch (RuntimeException e) {
                    exceptionCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });

        }
        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        assertEquals(2000 - (successCount.get() * 25) - (successCount.get() * FEE.amount()), accountFrom.getBalance());
        assertTrue(exceptionCount.get() > 0);
        assertTrue(accountFrom.getSpentToday() <= accountFrom.getDailyWithdrawalLimit());
    }

    @Test
    public void testTransactionHistoryAfterDeposit() {

        assertEquals(0, accountFrom.getTransactionHistory().size());

        accountFrom.deposit(2000);
        accountFrom.deposit(300);
        accountFrom.deposit(350);

        List<Transaction> transactionHistory = accountFrom.getTransactionHistory();
        assertEquals(3, transactionHistory.size());

        Transaction transaction = transactionHistory.get(0);
        assertEquals(transaction.fromAccount().getOwner().getId(), accountFrom.getOwner().getId());
        assertEquals(2000, transaction.amount());
        assertEquals(transaction.type().name(), TransactionType.DEPOSIT.name());
    }

    @Test
    public void testTransactionHistoryAfterWithdraw() {
        assertEquals(0, accountFrom.getTransactionHistory().size());

        accountFrom.deposit(1000);

        for(int i = 0; i < 5; i++){
            accountFrom.withdraw(15);
        }
        List<Transaction> transactionHistory = accountFrom.getTransactionHistory();

        assertEquals(11, transactionHistory.size());
        Transaction transaction = transactionHistory.get(1);

        assertEquals(transaction.fromAccount().getOwner().getId(), accountFrom.getOwner().getId());
        assertEquals(15, transaction.amount());
        assertEquals(transaction.type().name(), TransactionType.WITHDRAW.name());
    }
}
