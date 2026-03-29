package banking;

import java.time.OffsetDateTime;

public record Transaction (
        OffsetDateTime timestamp,
        TransactionType type,
        long amount,
        BankAccount fromAccount,
        BankAccount toAccount,
        long resultingBalance
) {
}
