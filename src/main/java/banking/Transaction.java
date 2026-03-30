package banking;

import java.time.LocalDate;

public record Transaction (
        LocalDate timestamp,
        TransactionType type,
        long amount,
        BankAccount fromAccount,
        BankAccount toAccount,
        long resultingBalance
) {
}
